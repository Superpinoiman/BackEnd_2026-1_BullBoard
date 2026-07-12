package com.bullboard.dto;

public class ArticleLikeResponse {

    private final long likeCount;
    private final boolean liked;

    public ArticleLikeResponse(long likeCount, boolean liked) {
        this.likeCount = likeCount;
        this.liked = liked;
    }

    public long getLikeCount() { return likeCount; }
    public boolean isLiked() { return liked; }
}
