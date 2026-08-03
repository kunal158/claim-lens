package com.claimlens.dto.tavily;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilyResult(
        String title,
        String url,
        String content,
        double score
) {
}
