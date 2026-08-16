package com.claimlens.controller;

import com.claimlens.domain.Reel;
import com.claimlens.domain.enums.ReelStatus;
import com.claimlens.dto.ClaimResponse;
import com.claimlens.dto.EvidenceResponse;
import com.claimlens.dto.ReelResponse;
import com.claimlens.dto.ReelUrlRequest;
import com.claimlens.dto.VerdictSummaryResponse;
import com.claimlens.repository.ClaimRepository;
import com.claimlens.repository.ReelRepository;
import com.claimlens.service.ReelPipelineService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reels")
public class ReelController {

    private final ReelRepository reelRepository;
    private final ClaimRepository claimRepository;
    private final ReelPipelineService reelPipelineService;
    private final Path uploadDir;

    public ReelController(ReelRepository reelRepository,
                           ClaimRepository claimRepository,
                           ReelPipelineService reelPipelineService,
                           @Value("${claimlens.upload-dir:./data/uploads}") String uploadDir) {
        this.reelRepository = reelRepository;
        this.claimRepository = claimRepository;
        this.reelPipelineService = reelPipelineService;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping
    public List<ReelResponse> list() {
        return reelRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ReelResponse::from)
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReelResponse> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty");
        }

        try {
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path destination = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            Reel reel = new Reel();
            reel.setSourceFilePath(destination.toString());
            reel = reelRepository.save(reel);

            return ResponseEntity.status(HttpStatus.CREATED).body(ReelResponse.from(reel));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to store uploaded file", e);
        }
    }

    @PostMapping(path = "/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReelResponse> fromUrl(@RequestBody ReelUrlRequest request) {
        String url = request.url();
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url must not be blank");
        }

        String trimmedUrl = trimUrl(url);

        Reel existing = reelRepository.findBySourceUrl(trimmedUrl).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(ReelResponse.from(existing));
        }

        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to prepare upload directory", e);
        }

        Reel reel = new Reel();
        reel.setSourceUrl(trimmedUrl);
        reel.setStatus(ReelStatus.DOWNLOADING);
        reel = reelRepository.save(reel);

        reelPipelineService.downloadAndProcess(reel.getId(), trimmedUrl, uploadDir);

        return ResponseEntity.status(HttpStatus.CREATED).body(ReelResponse.from(reel));
    }

    private static String trimUrl(String url) {
        String trimmed = url.trim();
        int queryIndex = trimmed.indexOf('?');
        return queryIndex == -1 ? trimmed : trimmed.substring(0, queryIndex);
    }

    @GetMapping("/{id}")
    public ReelResponse get(@PathVariable Long id) {
        Reel reel = reelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reel not found"));
        return ReelResponse.from(reel);
    }

    @PostMapping("/{id}/transcribe")
    public ReelResponse transcribe(@PathVariable Long id) {
        Reel reel = reelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reel not found"));
        return reelPipelineService.transcribe(reel);
    }

    @PostMapping("/{id}/extract-claims")
    public List<ClaimResponse> extractClaims(@PathVariable Long id) {
        Reel reel = reelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reel not found"));
        return reelPipelineService.extractClaims(reel);
    }

    @GetMapping("/{id}/claims")
    public List<ClaimResponse> getClaims(@PathVariable Long id) {
        if (!reelRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "reel not found");
        }
        return claimRepository.findByReelId(id).stream().map(ClaimResponse::from).toList();
    }

    @PostMapping("/{id}/retrieve-evidence")
    public List<EvidenceResponse> retrieveEvidence(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "false") boolean force) {
        Reel reel = reelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reel not found"));
        return reelPipelineService.retrieveEvidence(reel, force);
    }

    @PostMapping("/{id}/synthesize-verdicts")
    public VerdictSummaryResponse synthesizeVerdicts(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "false") boolean force) {
        Reel reel = reelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reel not found"));
        return reelPipelineService.synthesizeVerdicts(reel, force);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<ReelResponse> process(@PathVariable Long id) {
        Reel reel = reelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reel not found"));

        ReelStatus status = reel.getStatus();
        if (status == ReelStatus.DOWNLOADING
                || status == ReelStatus.TRANSCRIBING
                || status == ReelStatus.EXTRACTING_CLAIMS
                || status == ReelStatus.RETRIEVING_EVIDENCE
                || status == ReelStatus.SYNTHESIZING_VERDICTS
                || status == ReelStatus.VERDICTS_SYNTHESIZED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "reel cannot be processed from status " + status
                            + " — use the individual stage endpoints instead");
        }

        if (status == ReelStatus.FAILED) {
            reelPipelineService.resetForRetry(reel);
            if (reel.getStatus() == ReelStatus.DOWNLOADING) {
                reelPipelineService.downloadAndProcess(id, reel.getSourceUrl(), uploadDir);
                return ResponseEntity.accepted().body(ReelResponse.from(reel));
            }
        }

        reelPipelineService.processAsync(id);
        return ResponseEntity.accepted().body(ReelResponse.from(reel));
    }
}
