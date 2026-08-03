package com.claimlens.dto;

import com.claimlens.domain.Evidence;

import java.time.OffsetDateTime;

public record EvidenceResponse(
        Long id,
        Long claimId,
        String url,
        String title,
        String chunkText,
        Double tavilyScore,
        double similarityScore,
        OffsetDateTime createdAt
) {
    public static EvidenceResponse from(Evidence evidence) {
        return new EvidenceResponse(
                evidence.getId(),
                evidence.getClaimId(),
                evidence.getUrl(),
                evidence.getTitle(),
                evidence.getChunkText(),
                evidence.getTavilyScore(),
                evidence.getSimilarityScore(),
                evidence.getCreatedAt()
        );
    }
}
