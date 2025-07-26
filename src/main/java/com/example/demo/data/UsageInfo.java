package com.example.demo.data;

import lombok.Data;

@Data
public class UsageInfo {
  public String apiKey;
  public int usage;
  public int remaining;
  public long ttl;

  public UsageInfo(String apiKey, int usage, int remaining, long ttl) {
    this.apiKey = apiKey;
    this.usage = usage;
    this.remaining = remaining;
    this.ttl = ttl;
  }
}
