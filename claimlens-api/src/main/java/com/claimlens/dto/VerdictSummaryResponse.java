package com.claimlens.dto;

import java.util.List;

public record VerdictSummaryResponse(
        Long reelId,
        Double trustScore,
        String summary,
        List<ClaimVerdictResponse> verdicts
) {
}
