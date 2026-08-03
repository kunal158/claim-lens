package com.claimlens.dto.tavily;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilySearchResponse(List<TavilyResult> results) {
}
