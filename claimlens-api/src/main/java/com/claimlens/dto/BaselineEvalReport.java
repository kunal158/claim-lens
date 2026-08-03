package com.claimlens.dto;

import java.util.List;

public record BaselineEvalReport(
        int totalClaims,
        int correct,
        double accuracyPct,
        List<MismatchDetail> mismatches
) {
}
