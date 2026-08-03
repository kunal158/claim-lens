package com.claimlens.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiFile(
        String name,
        String uri,
        String mimeType,
        String state
) {
}
