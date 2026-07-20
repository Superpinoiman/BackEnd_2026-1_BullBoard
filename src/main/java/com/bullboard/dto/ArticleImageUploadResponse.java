package com.bullboard.dto;

import java.util.Map;

public class ArticleImageUploadResponse {

    private final String objectKey;
    private final String uploadUrl;
    private final Map<String, String> headers;
    private final long expiresInSeconds;

    public ArticleImageUploadResponse(String objectKey, String uploadUrl,
                                      Map<String, String> headers, long expiresInSeconds) {
        this.objectKey = objectKey;
        this.uploadUrl = uploadUrl;
        this.headers = headers;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getObjectKey() { return objectKey; }
    public String getUploadUrl() { return uploadUrl; }
    public Map<String, String> getHeaders() { return headers; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
}
