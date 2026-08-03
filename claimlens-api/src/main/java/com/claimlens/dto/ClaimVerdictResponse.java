package com.claimlens.dto;

import com.claimlens.domain.ClaimVerdict;
import com.claimlens.domain.enums.Verdict;

import java.time.OffsetDateTime;

public record ClaimVerdictResponse(
        Long id,
        Long claimId,
        Verdict verdict,
        double confidence,
        String reasoning,
        OffsetDateTime createdAt
) {
    public static ClaimVerdictResponse from(ClaimVerdict claimVerdict) {
        return new ClaimVerdictResponse(
                claimVerdict.getId(),
                claimVerdict.getClaimId(),
                claimVerdict.getVerdict(),
                claimVerdict.getConfidence(),
                claimVerdict.getReasoning(),
                claimVerdict.getCreatedAt()
        );
    }
}
