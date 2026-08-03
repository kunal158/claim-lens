package com.claimlens.dto;

import com.claimlens.domain.enums.Verdict;

public record MismatchDetail(
        Long claimId,
        String claimText,
        Verdict humanLabel,
        Verdict modelVerdict,
        String modelReasoning
) {
}
