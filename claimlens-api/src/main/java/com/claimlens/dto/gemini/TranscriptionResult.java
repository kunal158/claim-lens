package com.claimlens.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TranscriptionResult(
        @JsonProperty("transcript_segments") List<TimedTextSegment> transcriptSegments,
        @JsonProperty("onscreen_text_segments") List<TimedTextSegment> onscreenTextSegments
) {
}
