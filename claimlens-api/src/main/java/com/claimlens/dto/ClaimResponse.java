package com.claimlens.dto;

import com.claimlens.domain.Claim;
import com.claimlens.domain.enums.ClaimSource;

import java.time.OffsetDateTime;

public record ClaimResponse(
        Long id,
        Long reelId,
        String claimText,
        String searchQuery,
        ClaimSource source,
        double startS,
        double endS,
        OffsetDateTime createdAt
) {
    public static ClaimResponse from(Claim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getReelId(),
                claim.getClaimText(),
                claim.getSearchQuery(),
                claim.getSource(),
                claim.getStartS(),
                claim.getEndS(),
                claim.getCreatedAt()
        );
    }
}
