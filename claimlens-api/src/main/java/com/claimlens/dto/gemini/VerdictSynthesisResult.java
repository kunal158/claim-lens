package com.claimlens.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VerdictSynthesisResult(
        @JsonProperty("verdicts") List<ClaimVerdictItem> verdicts,
        @JsonProperty("summary") String summary
) {
}
