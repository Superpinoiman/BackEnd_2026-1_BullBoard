package com.bullboard.service;

import com.bullboard.dto.FearGreedResponse;
import com.bullboard.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
public class MarketSentimentService {

    private static final Logger log = LoggerFactory.getLogger(MarketSentimentService.class);
    private static final Duration CACHE_DURATION = Duration.ofMinutes(5);
    private static final String SOURCE = "CNN Fear & Greed Index";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI endpoint;
    private volatile CacheEntry cache;

    public MarketSentimentService(
            ObjectMapper objectMapper,
            @Value("${app.market.fear-greed-url:https://production.dataviz.cnn.io/index/fearandgreed/graphdata}")
            String endpoint
    ) {
        this.objectMapper = objectMapper;
        this.endpoint = URI.create(endpoint);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public FearGreedResponse getFearGreed() {
        CacheEntry current = cache;
        Instant now = Instant.now();

        if (current != null && current.expiresAt().isAfter(now)) {
            return current.response();
        }

        synchronized (this) {
            current = cache;
            now = Instant.now();
            if (current != null && current.expiresAt().isAfter(now)) {
                return current.response();
            }

            try {
                FearGreedResponse response = requestFearGreed();
                cache = new CacheEntry(response, now.plus(CACHE_DURATION));
                return response;
            } catch (IOException e) {
                return useStaleCacheOrThrow(current, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return useStaleCacheOrThrow(current, e);
            } catch (RuntimeException e) {
                return useStaleCacheOrThrow(current, e);
            }
        }
    }

    private FearGreedResponse requestFearGreed() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .header("Referer", "https://edition.cnn.com/markets/fear-and-greed")
                .header("Origin", "https://edition.cnn.com")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 Chrome/138.0.0.0 Safari/537.36")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Fear & Greed provider returned HTTP " + response.statusCode());
        }

        return parseResponse(objectMapper.readTree(response.body()));
    }

    FearGreedResponse parseResponse(JsonNode root) {
        JsonNode index = root.path("fear_and_greed");
        if (index.isMissingNode() || !index.hasNonNull("score")
                || !index.hasNonNull("rating") || !index.hasNonNull("timestamp")) {
            throw new IllegalStateException("Fear & Greed response is missing required fields");
        }

        double rawScore = index.path("score").asDouble(Double.NaN);
        if (!Double.isFinite(rawScore) || rawScore < 0 || rawScore > 100) {
            throw new IllegalStateException("Fear & Greed score is invalid");
        }

        String rating = index.path("rating").asText().trim().toLowerCase(Locale.ROOT);
        Instant updatedAt;
        try {
            updatedAt = Instant.parse(index.path("timestamp").asText());
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Fear & Greed timestamp is invalid", e);
        }

        return new FearGreedResponse(
                (int) Math.round(rawScore),
                rating,
                translateRating(rating),
                updatedAt,
                index.path("previous_close").asDouble(rawScore),
                index.path("previous_1_week").asDouble(rawScore),
                index.path("previous_1_month").asDouble(rawScore),
                false,
                SOURCE
        );
    }

    private FearGreedResponse useStaleCacheOrThrow(CacheEntry current, Exception error) {
        log.warn("공포·탐욕지수 조회에 실패했습니다.", error);
        if (current != null) {
            return current.response().asStale();
        }
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String translateRating(String rating) {
        return switch (rating) {
            case "extreme fear" -> "극도의 공포";
            case "fear" -> "공포";
            case "neutral" -> "중립";
            case "greed" -> "탐욕";
            case "extreme greed" -> "극도의 탐욕";
            default -> "시장 심리";
        };
    }

    private record CacheEntry(FearGreedResponse response, Instant expiresAt) {
    }
}
