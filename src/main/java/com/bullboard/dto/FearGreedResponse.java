package com.bullboard.dto;

import java.time.Instant;

public record FearGreedResponse(
        int score,
        String rating,
        String ratingLabel,
        Instant updatedAt,
        double previousClose,
        double previousWeek,
        double previousMonth,
        boolean stale,
        String source
) {

    public FearGreedResponse asStale() {
        return new FearGreedResponse(
                score,
                rating,
                ratingLabel,
                updatedAt,
                previousClose,
                previousWeek,
                previousMonth,
                true,
                source
        );
    }
}
