package com.example.demo.persistence.model;

public class RateLimitModel {
    private String apiKey;
    private int limit;
    private int windowSeconds;

    public RateLimitModel() {}

    public RateLimitModel(String apiKey, int limit, int windowSeconds) {
        this.apiKey = apiKey;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}