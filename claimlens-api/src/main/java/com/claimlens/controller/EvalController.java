package com.claimlens.controller;

import com.claimlens.domain.Claim;
import com.claimlens.domain.ClaimVerdict;
import com.claimlens.domain.EvalLabel;
import com.claimlens.domain.EvalResult;
import com.claimlens.domain.enums.Verdict;
import com.claimlens.dto.BaselineEvalReport;
import com.claimlens.dto.EvalLabelRequest;
import com.claimlens.dto.MismatchDetail;
import com.claimlens.dto.gemini.NoRetrievalVerdict;
import com.claimlens.repository.ClaimRepository;
import com.claimlens.repository.ClaimVerdictRepository;
import com.claimlens.repository.EvalLabelRepository;
import com.claimlens.repository.EvalResultRepository;
import com.claimlens.service.GeminiClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class EvalController {

    private static final String NO_RETRIEVAL_BASELINE = "NO_RETRIEVAL_BASELINE";
    private static final String RETRIEVAL_BACKED = "RETRIEVAL_BACKED";

    private final ClaimRepository claimRepository;
    private final EvalLabelRepository evalLabelRepository;
    private final EvalResultRepository evalResultRepository;
    private final ClaimVerdictRepository claimVerdictRepository;
    private final GeminiClient geminiClient;

    public EvalController(ClaimRepository claimRepository,
                           EvalLabelRepository evalLabelRepository,
                           EvalResultRepository evalResultRepository,
                           ClaimVerdictRepository claimVerdictRepository,
                           GeminiClient geminiClient) {
        this.claimRepository = claimRepository;
        this.evalLabelRepository = evalLabelRepository;
        this.evalResultRepository = evalResultRepository;
        this.claimVerdictRepository = claimVerdictRepository;
        this.geminiClient = geminiClient;
    }

    @PostMapping("/claims/{id}/label")
    public EvalLabel label(@PathVariable Long id, @RequestBody EvalLabelRequest request) {
        if (!claimRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "claim not found");
        }

        Verdict humanLabel = parseVerdict(request.humanLabel());

        EvalLabel evalLabel = evalLabelRepository.findByClaimId(id).orElseGet(EvalLabel::new);
        evalLabel.setClaimId(id);
        evalLabel.setHumanLabel(humanLabel);
        evalLabel.setNotes(request.notes());
        return evalLabelRepository.save(evalLabel);
    }

    @PostMapping("/eval/baseline")
    public BaselineEvalReport runBaseline(@RequestParam(defaultValue = "false") boolean force) {
        List<EvalLabel> labels = evalLabelRepository.findAll();
        if (labels.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "no eval labels exist yet");
        }

        List<Long> claimIds = labels.stream().map(EvalLabel::getClaimId).toList();

        if (force) {
            evalResultRepository.deleteByRunTypeAndClaimIdIn(NO_RETRIEVAL_BASELINE, claimIds);
        }

        Map<Long, EvalResult> existingByClaimId = evalResultRepository
                .findByRunTypeAndClaimIdIn(NO_RETRIEVAL_BASELINE, claimIds).stream()
                .collect(Collectors.toMap(EvalResult::getClaimId, r -> r));

        boolean firstCall = true;
        for (EvalLabel label : labels) {
            if (existingByClaimId.containsKey(label.getClaimId())) {
                continue;
            }

            if (!firstCall) {
                sleepForRateLimit();
            }
            firstCall = false;

            Claim claim = claimRepository.findById(label.getClaimId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "eval label references missing claim " + label.getClaimId()));

            NoRetrievalVerdict verdict = judgeWithRetry(claim.getClaimText());
            Verdict modelVerdict = parseVerdict(verdict.verdict());

            EvalResult result = new EvalResult();
            result.setClaimId(claim.getId());
            result.setRunType(NO_RETRIEVAL_BASELINE);
            result.setModelVerdict(modelVerdict);
            result.setModelReasoning(verdict.reasoning());
            evalResultRepository.save(result);

            existingByClaimId.put(claim.getId(), result);
        }

        return buildReport(labels, existingByClaimId);
    }

    @PostMapping("/eval/retrieval-backed")
    public BaselineEvalReport runRetrievalBacked(@RequestParam(defaultValue = "false") boolean force) {
        List<EvalLabel> labels = evalLabelRepository.findAll();
        if (labels.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "no eval labels exist yet");
        }

        List<Long> claimIds = labels.stream().map(EvalLabel::getClaimId).toList();

        if (force) {
            evalResultRepository.deleteByRunTypeAndClaimIdIn(RETRIEVAL_BACKED, claimIds);
        }

        Map<Long, EvalResult> existingByClaimId = evalResultRepository
                .findByRunTypeAndClaimIdIn(RETRIEVAL_BACKED, claimIds).stream()
                .collect(Collectors.toMap(EvalResult::getClaimId, r -> r));

        for (EvalLabel label : labels) {
            if (existingByClaimId.containsKey(label.getClaimId())) {
                continue;
            }

            ClaimVerdict claimVerdict = claimVerdictRepository.findByClaimId(label.getClaimId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                            "claim " + label.getClaimId()
                                    + " has no verdict yet — run synthesize-verdicts on its reel first"));

            EvalResult result = new EvalResult();
            result.setClaimId(label.getClaimId());
            result.setRunType(RETRIEVAL_BACKED);
            result.setModelVerdict(claimVerdict.getVerdict());
            result.setModelReasoning(claimVerdict.getReasoning());
            evalResultRepository.save(result);

            existingByClaimId.put(label.getClaimId(), result);
        }

        return buildReport(labels, existingByClaimId);
    }

    private BaselineEvalReport buildReport(List<EvalLabel> labels, Map<Long, EvalResult> resultsByClaimId) {
        int correct = 0;
        List<MismatchDetail> mismatches = new ArrayList<>();
        for (EvalLabel label : labels) {
            EvalResult result = resultsByClaimId.get(label.getClaimId());
            if (result.getModelVerdict() == label.getHumanLabel()) {
                correct++;
            } else {
                Claim claim = claimRepository.findById(label.getClaimId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "eval label references missing claim " + label.getClaimId()));
                mismatches.add(new MismatchDetail(claim.getId(), claim.getClaimText(),
                        label.getHumanLabel(), result.getModelVerdict(), result.getModelReasoning()));
            }
        }

        double accuracyPct = 100.0 * correct / labels.size();
        return new BaselineEvalReport(labels.size(), correct, accuracyPct, mismatches);
    }

    private NoRetrievalVerdict judgeWithRetry(String claimText) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return geminiClient.judgeClaimNoRetrieval(claimText);
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
                    "interrupted while rate-limiting Gemini calls");
        }
    }

    private Verdict parseVerdict(String raw) {
        if (raw == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "verdict must not be null");
        }
        try {
            return Verdict.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid verdict: " + raw);
        }
    }
}
