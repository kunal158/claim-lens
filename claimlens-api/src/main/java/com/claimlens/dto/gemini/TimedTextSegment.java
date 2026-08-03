package com.claimlens.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TimedTextSegment(
        @JsonProperty("start_s") double startS,
        @JsonProperty("end_s") double endS,
        @JsonProperty("text") String text
) {
}
