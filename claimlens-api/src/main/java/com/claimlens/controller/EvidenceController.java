package com.claimlens.controller;

import com.claimlens.dto.EvidenceResponse;
import com.claimlens.repository.ClaimRepository;
import com.claimlens.repository.EvidenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EvidenceController {

    private final ClaimRepository claimRepository;
    private final EvidenceRepository evidenceRepository;

    public EvidenceController(ClaimRepository claimRepository, EvidenceRepository evidenceRepository) {
        this.claimRepository = claimRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @GetMapping("/claims/{id}/evidence")
    public List<EvidenceResponse> getEvidence(@PathVariable Long id) {
        if (!claimRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "claim not found");
        }
        return evidenceRepository.findByClaimIdOrderBySimilarityScoreDesc(id).stream()
                .map(EvidenceResponse::from)
                .toList();
    }
}
