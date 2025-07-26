package com.example.demo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitMessageData {
    private String action; // e.g. "REFRESH", "DELETE"
    private String apiKey;
}