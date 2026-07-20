package com.bullboard.controller;

import com.bullboard.dto.FearGreedResponse;
import com.bullboard.service.MarketSentimentService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketSentimentService marketSentimentService;

    public MarketController(MarketSentimentService marketSentimentService) {
        this.marketSentimentService = marketSentimentService;
    }

    @GetMapping("/fear-greed")
    public ResponseEntity<FearGreedResponse> getFearGreed() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(marketSentimentService.getFearGreed());
    }
}
