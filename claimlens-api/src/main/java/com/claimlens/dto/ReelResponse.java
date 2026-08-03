package com.claimlens.dto;

import com.claimlens.domain.Reel;
import com.claimlens.domain.enums.ReelStatus;

import java.time.OffsetDateTime;

public record ReelResponse(
        Long id,
        ReelStatus status,
        String sourceFilePath,
        String sourceUrl,
        String transcriptSegments,
        String onscreenTextSegments,
        Double trustScore,
        String summary,
        OffsetDateTime createdAt
) {
    public static ReelResponse from(Reel reel) {
        return new ReelResponse(
                reel.getId(),
                reel.getStatus(),
                reel.getSourceFilePath(),
                reel.getSourceUrl(),
                reel.getTranscriptSegments(),
                reel.getOnscreenTextSegments(),
                reel.getTrustScore(),
                reel.getSummary(),
                reel.getCreatedAt()
        );
    }
}
