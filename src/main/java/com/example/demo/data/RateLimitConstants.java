package com.example.demo.data;

public class RateLimitConstants {
  public static final String REDIS_KEY_PREFIX = "rate:";
  
  public static String buildRedisKey(String apiKey) {
    return REDIS_KEY_PREFIX + apiKey;
  }
}