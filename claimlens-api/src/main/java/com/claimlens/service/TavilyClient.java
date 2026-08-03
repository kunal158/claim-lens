package com.claimlens.service;

import com.claimlens.config.TavilyProperties;
import com.claimlens.dto.tavily.TavilyResult;
import com.claimlens.dto.tavily.TavilySearchResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class TavilyClient {

    private final RestClient restClient;
    private final TavilyProperties properties;

    public TavilyClient(RestClient.Builder restClientBuilder, TavilyProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    public List<TavilyResult> search(String query) {
        Map<String, Object> requestBody = Map.of(
                "query", query,
                "search_depth", "basic",
                "max_results", 5,
                "include_answer", false,
                "include_raw_content", false
        );

        TavilySearchResponse response = restClient.post()
                .uri(properties.getBaseUrl() + "/search")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(TavilySearchResponse.class);

        if (response == null || response.results() == null) {
            throw new IllegalStateException("Tavily returned no results for query: " + query);
        }
        return response.results();
    }
}
