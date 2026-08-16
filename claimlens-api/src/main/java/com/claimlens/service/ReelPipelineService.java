package com.claimlens.service;

import com.claimlens.domain.Claim;
import com.claimlens.domain.ClaimVerdict;
import com.claimlens.domain.Evidence;
import com.claimlens.domain.Reel;
import com.claimlens.domain.enums.ClaimSource;
import com.claimlens.domain.enums.ReelStatus;
import com.claimlens.domain.enums.Verdict;
import com.claimlens.dto.ClaimResponse;
import com.claimlens.dto.ClaimVerdictResponse;
import com.claimlens.dto.EvidenceResponse;
import com.claimlens.dto.ReelResponse;
import com.claimlens.dto.VerdictSummaryResponse;
import com.claimlens.dto.gemini.ClaimExtractionResult;
import com.claimlens.dto.gemini.ClaimVerdictItem;
import com.claimlens.dto.gemini.ExtractedClaim;
import com.claimlens.dto.gemini.GeminiFile;
import com.claimlens.dto.gemini.TimedTextSegment;
import com.claimlens.dto.gemini.TranscriptionResult;
import com.claimlens.dto.gemini.VerdictSynthesisResult;
import com.claimlens.dto.tavily.TavilyResult;
import com.claimlens.repository.ClaimRepository;
import com.claimlens.repository.ClaimVerdictRepository;
import com.claimlens.repository.EvidenceRepository;
import com.claimlens.repository.ReelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Service
public class ReelPipelineService {

    private static final Logger log = LoggerFactory.getLogger(ReelPipelineService.class);

    private final ReelRepository reelRepository;
    private final ClaimRepository claimRepository;
    private final EvidenceRepository evidenceRepository;
    private final ClaimVerdictRepository claimVerdictRepository;
    private final GeminiClient geminiClient;
    private final TavilyClient tavilyClient;
    private final YtDlpService ytDlpService;
    private final ObjectMapper objectMapper;

    public ReelPipelineService(ReelRepository reelRepository,
                               ClaimRepository claimRepository,
                               EvidenceRepository evidenceRepository,
                               ClaimVerdictRepository claimVerdictRepository,
                               GeminiClient geminiClient,
                               TavilyClient tavilyClient,
                               YtDlpService ytDlpService,
                               ObjectMapper objectMapper) {
        this.reelRepository = reelRepository;
        this.claimRepository = claimRepository;
        this.evidenceRepository = evidenceRepository;
        this.claimVerdictRepository = claimVerdictRepository;
        this.geminiClient = geminiClient;
        this.tavilyClient = tavilyClient;
        this.ytDlpService = ytDlpService;
        this.objectMapper = objectMapper;
    }

    @Async("reelPipelineExecutor")
    public void downloadAndProcess(Long reelId, String url, Path uploadDir) {
        Reel reel = reelRepository.findById(reelId).orElse(null);
        if (reel == null) {
            log.warn("reel {} not found when starting async download", reelId);
            return;
        }

        try {
            Path downloaded = ytDlpService.download(url, uploadDir);
            reel.setSourceFilePath(downloaded.toString());
            reel.setStatus(ReelStatus.PENDING);
            reelRepository.save(reel);
        } catch (RuntimeException e) {
            log.error("download failed for reel {}", reelId, e);
            reel.setStatus(ReelStatus.FAILED);
            reelRepository.save(reel);
            return;
        }

        processAsync(reelId);
    }

    public ReelResponse transcribe(Reel reel) {
        reel.setStatus(ReelStatus.TRANSCRIBING);
        reelRepository.save(reel);

        try {
            GeminiFile uploaded = geminiClient.uploadFile(Path.of(reel.getSourceFilePath()));
            GeminiFile active = geminiClient.waitUntilActive(uploaded.name());
            TranscriptionResult result = geminiClient.transcribe(active);

            reel.setTranscriptSegments(objectMapper.writeValueAsString(result.transcriptSegments()));
            reel.setOnscreenTextSegments(objectMapper.writeValueAsString(result.onscreenTextSegments()));
            reel.setStatus(ReelStatus.TRANSCRIBED);
            reel = reelRepository.save(reel);

            return ReelResponse.from(reel);
        } catch (RuntimeException e) {
            reel.setStatus(ReelStatus.FAILED);
            reelRepository.save(reel);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "transcription failed", e);
        }
    }

    public List<ClaimResponse> extractClaims(Reel reel) {
        if (reel.getStatus() != ReelStatus.TRANSCRIBED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "reel must be TRANSCRIBED before extracting claims, current status: " + reel.getStatus());
        }

        reel.setStatus(ReelStatus.EXTRACTING_CLAIMS);
        reelRepository.save(reel);

        try {
            List<TimedTextSegment> transcriptSegments = Arrays.asList(
                    objectMapper.readValue(reel.getTranscriptSegments(), TimedTextSegment[].class));
            List<TimedTextSegment> onscreenTextSegments = Arrays.asList(
                    objectMapper.readValue(reel.getOnscreenTextSegments(), TimedTextSegment[].class));

            ClaimExtractionResult result = geminiClient.extractClaims(transcriptSegments, onscreenTextSegments);

            List<Claim> claims = result.claims().stream()
                    .map(extracted -> toClaimEntity(extracted, reel.getId()))
                    .toList();
            claims = claimRepository.saveAll(claims);

            reel.setStatus(ReelStatus.CLAIMS_EXTRACTED);
            reelRepository.save(reel);

            return claims.stream().map(ClaimResponse::from).toList();
        } catch (RuntimeException e) {
            reel.setStatus(ReelStatus.FAILED);
            reelRepository.save(reel);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "claim extraction failed", e);
        }
    }

    public List<EvidenceResponse> retrieveEvidence(Reel reel, boolean force) {
        if (reel.getStatus() != ReelStatus.CLAIMS_EXTRACTED && reel.getStatus() != ReelStatus.EVIDENCE_RETRIEVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "reel must have claims extracted before retrieving evidence, current status: " + reel.getStatus());
        }

        List<Claim> claims = claimRepository.findByReelId(reel.getId());
        List<Long> claimIds = claims.stream().map(Claim::getId).toList();

        reel.setStatus(ReelStatus.RETRIEVING_EVIDENCE);
        reelRepository.save(reel);

        if (force) {
            evidenceRepository.deleteByClaimIdIn(claimIds);
        }

        try {
            if (claims.stream().anyMatch(c -> c.getSearchQuery() == null)) {
                List<String> claimTexts = claims.stream().map(Claim::getClaimText).toList();
                List<String> searchQueries = withRetry(() -> geminiClient.rewriteSearchQueries(claimTexts));
                if (searchQueries.size() != claims.size()) {
                    throw new IllegalStateException("search query rewrite returned " + searchQueries.size()
                            + " queries for " + claims.size() + " claims");
                }
                for (int i = 0; i < claims.size(); i++) {
                    claims.get(i).setSearchQuery(searchQueries.get(i));
                }
                claims = claimRepository.saveAll(claims);
            }

            boolean firstCall = true;
            for (Claim claim : claims) {
                if (!force && evidenceRepository.existsByClaimId(claim.getId())) {
                    continue;
                }

                if (!firstCall) {
                    sleepForRateLimit();
                }
                firstCall = false;

                List<TavilyResult> results = withRetry(() -> tavilyClient.search(claim.getSearchQuery()));
                if (results.isEmpty()) {
                    continue;
                }

                List<String> contents = results.stream().map(TavilyResult::content).toList();
                List<List<Double>> evidenceEmbeddings = withRetry(
                        () -> geminiClient.batchEmbedContents(contents, "RETRIEVAL_DOCUMENT"));
                List<Double> claimEmbedding = withRetry(
                        () -> geminiClient.embedContent(claim.getClaimText(), "RETRIEVAL_QUERY"));

                List<Evidence> evidenceRows = new ArrayList<>();
                for (int i = 0; i < results.size(); i++) {
                    TavilyResult result = results.get(i);
                    List<Double> embedding = evidenceEmbeddings.get(i);

                    Evidence evidence = new Evidence();
                    evidence.setClaimId(claim.getId());
                    evidence.setUrl(result.url());
                    evidence.setTitle(result.title());
                    evidence.setChunkText(result.content());
                    evidence.setTavilyScore(result.score());
                    evidence.setEmbedding(objectMapper.writeValueAsString(embedding));
                    evidence.setSimilarityScore(CosineSimilarity.of(claimEmbedding, embedding));
                    evidenceRows.add(evidence);
                }
                evidenceRepository.saveAll(evidenceRows);
            }

            reel.setStatus(ReelStatus.EVIDENCE_RETRIEVED);
            reelRepository.save(reel);

            return claims.stream()
                    .flatMap(claim -> evidenceRepository.findByClaimIdOrderBySimilarityScoreDesc(claim.getId()).stream())
                    .map(EvidenceResponse::from)
                    .toList();
        } catch (RuntimeException e) {
            reel.setStatus(ReelStatus.FAILED);
            reelRepository.save(reel);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "evidence retrieval failed", e);
        }
    }

    public VerdictSummaryResponse synthesizeVerdicts(Reel reel, boolean force) {
        if (reel.getStatus() != ReelStatus.EVIDENCE_RETRIEVED && reel.getStatus() != ReelStatus.VERDICTS_SYNTHESIZED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "reel must have evidence retrieved before synthesizing verdicts, current status: " + reel.getStatus());
        }

        List<Claim> claims = claimRepository.findByReelId(reel.getId());
        List<Long> claimIds = claims.stream().map(Claim::getId).toList();

        reel.setStatus(ReelStatus.SYNTHESIZING_VERDICTS);
        reelRepository.save(reel);

        if (force) {
            claimVerdictRepository.deleteByClaimIdIn(claimIds);
        }

        try {
            boolean anyMissing = claims.stream().anyMatch(c -> !claimVerdictRepository.existsByClaimId(c.getId()));
            if (force || anyMissing) {
                List<String> claimTexts = claims.stream().map(Claim::getClaimText).toList();
                List<List<Evidence>> evidenceLists = claims.stream()
                        .map(c -> evidenceRepository.findByClaimIdOrderBySimilarityScoreDesc(c.getId())
                                .stream().limit(3).toList())
                        .toList();

                VerdictSynthesisResult result = withRetry(
                        () -> geminiClient.synthesizeVerdicts(claimTexts, evidenceLists));
                if (result.verdicts().size() != claims.size()) {
                    throw new IllegalStateException("verdict synthesis returned " + result.verdicts().size()
                            + " verdicts for " + claims.size() + " claims");
                }

                List<ClaimVerdict> toSave = new ArrayList<>();
                for (int i = 0; i < claims.size(); i++) {
                    ClaimVerdictItem item = result.verdicts().get(i);
                    ClaimVerdict claimVerdict = new ClaimVerdict();
                    claimVerdict.setClaimId(claims.get(i).getId());
                    claimVerdict.setVerdict(Verdict.valueOf(item.verdict().toUpperCase()));
                    claimVerdict.setConfidence(item.confidence());
                    claimVerdict.setReasoning(item.reasoning());
                    toSave.add(claimVerdict);
                }
                claimVerdictRepository.saveAll(toSave);
                reel.setSummary(result.summary());
            }

            List<ClaimVerdict> verdicts = claimVerdictRepository.findByClaimIdIn(claimIds);
            reel.setTrustScore(computeTrustScore(verdicts));
            reel.setStatus(ReelStatus.VERDICTS_SYNTHESIZED);
            reelRepository.save(reel);

            return new VerdictSummaryResponse(reel.getId(), reel.getTrustScore(), reel.getSummary(),
                    verdicts.stream().map(ClaimVerdictResponse::from).toList());
        } catch (RuntimeException e) {
            reel.setStatus(ReelStatus.FAILED);
            reelRepository.save(reel);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "verdict synthesis failed", e);
        }
    }

    public void resetForRetry(Reel reel) {
        List<Claim> claims = claimRepository.findByReelId(reel.getId());
        List<Long> claimIds = claims.stream().map(Claim::getId).toList();
        if (!claimIds.isEmpty()) {
            claimVerdictRepository.deleteByClaimIdIn(claimIds);
            evidenceRepository.deleteByClaimIdIn(claimIds);
            claimRepository.deleteAll(claims);
        }

        reel.setTranscriptSegments(null);
        reel.setOnscreenTextSegments(null);
        reel.setTrustScore(null);
        reel.setSummary(null);
        reel.setStatus(reel.getSourceFilePath() == null ? ReelStatus.DOWNLOADING : ReelStatus.PENDING);
        reelRepository.save(reel);
    }

    @Async("reelPipelineExecutor")
    public void processAsync(Long reelId) {
        Reel reel = reelRepository.findById(reelId).orElse(null);
        if (reel == null) {
            log.warn("reel {} not found when starting async pipeline", reelId);
            return;
        }

        try {
            if (reel.getStatus() == ReelStatus.PENDING) {
                transcribe(reel);
            }
            if (reel.getStatus() == ReelStatus.TRANSCRIBED) {
                extractClaims(reel);
            }
            if (reel.getStatus() == ReelStatus.CLAIMS_EXTRACTED) {
                retrieveEvidence(reel, false);
            }
            if (reel.getStatus() == ReelStatus.EVIDENCE_RETRIEVED) {
                synthesizeVerdicts(reel, false);
            }
        } catch (RuntimeException e) {
            log.error("async pipeline processing failed for reel {}", reelId, e);
        }
    }

    private Double computeTrustScore(List<ClaimVerdict> verdicts) {
        long trueCount = verdicts.stream().filter(v -> v.getVerdict() == Verdict.TRUE).count();
        long falseCount = verdicts.stream().filter(v -> v.getVerdict() == Verdict.FALSE).count();
        if (trueCount + falseCount == 0) {
            return null;
        }
        return Math.round(100.0 * trueCount / (trueCount + falseCount) * 100.0) / 100.0;
    }

    private <T> T withRetry(Supplier<T> supplier) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (HttpServerErrorException e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                sleepFor(10_000L * attempt);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private void sleepForRateLimit() {
        sleepFor(13_000);
    }

    private void sleepFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "interrupted while rate-limiting evidence retrieval calls");
        }
    }

    private Claim toClaimEntity(ExtractedClaim extracted, Long reelId) {
        Claim claim = new Claim();
        claim.setReelId(reelId);
        claim.setClaimText(extracted.claimText());
        claim.setSource(ClaimSource.valueOf(extracted.source().toUpperCase()));
        claim.setStartS(extracted.startS());
        claim.setEndS(extracted.endS());
        return claim;
    }
}
