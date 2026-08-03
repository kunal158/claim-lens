package com.claimlens.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClaimVerdictItem(
        @JsonProperty("verdict") String verdict,
        @JsonProperty("confidence") double confidence,
        @JsonProperty("reasoning") String reasoning
) {
}
