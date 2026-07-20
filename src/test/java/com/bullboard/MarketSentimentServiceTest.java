package com.bullboard.service;

import com.bullboard.dto.FearGreedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketSentimentServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MarketSentimentService service = new MarketSentimentService(
            objectMapper, "https://example.com/fear-greed");

    @Test
    void parsesFearGreedResponse() throws Exception {
        String json = """
                {
                  "fear_and_greed": {
                    "score": 39.514,
                    "rating": "fear",
                    "timestamp": "2026-07-20T17:01:18Z",
                    "previous_close": 37.057,
                    "previous_1_week": 40.857,
                    "previous_1_month": 37.343
                  }
                }
                """;

        FearGreedResponse response = service.parseResponse(objectMapper.readTree(json));

        assertThat(response.score()).isEqualTo(40);
        assertThat(response.rating()).isEqualTo("fear");
        assertThat(response.ratingLabel()).isEqualTo("공포");
        assertThat(response.previousClose()).isEqualTo(37.057);
        assertThat(response.stale()).isFalse();
    }
}
