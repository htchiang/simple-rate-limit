package com.example.demo.controller;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.data.ApiResponse;
import com.example.demo.data.UsageInfo;
import com.example.demo.persistence.model.RateLimitModel;
import com.example.demo.service.RateLimitService;
import com.example.demo.service.RateLimitService.PagedResult;

@RestController
@RequestMapping("/")
public class RateLimitController {

  private final RateLimitService service;

  public RateLimitController(RateLimitService service) {
    this.service = service;
  }

  @PostMapping("limits")
  public ResponseEntity<ApiResponse<Void>> define(@RequestBody RateLimitModel rateLimit) {
    service.defineLimit(rateLimit);
    return ResponseEntity.ok(ApiResponse.ok(null));
  }

  @DeleteMapping("limits/{apiKey}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String apiKey) {
    service.deleteLimit(apiKey);
    return ResponseEntity.ok(ApiResponse.ok(null));
  }

  @GetMapping("check")
  public CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> check(
      @RequestParam String apiKey) {
    return service.checkAccess(apiKey).thenCompose(allowed -> {
      if (allowed) {
        return CompletableFuture
            .completedFuture(ResponseEntity.ok(ApiResponse.ok(Map.of("allowed", true))));
      } else {
        return service.getUsage(apiKey).thenApply(usage -> {
          long retryAfter = (usage != null && usage.getTtl() > 0) ? usage.getTtl() : 60L;
          return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
              .header("Retry-After", String.valueOf(retryAfter)).body(ApiResponse.error(429,
                  "Rate limit exceeded", Map.of("allowed", false, "retryAfter", retryAfter)));
        });
      }
    });
  }

  @GetMapping("usage")
  public CompletableFuture<ResponseEntity<ApiResponse<UsageInfo>>> usage(
      @RequestParam String apiKey) {
    return service.getUsage(apiKey)
        .thenApply(info -> info != null ? ResponseEntity.ok(ApiResponse.ok(info))
            : ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "API Key not found", null)));
  }

  @GetMapping("limits")
  public ResponseEntity<ApiResponse<PagedResult<RateLimitModel>>> getAllLimits(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(ApiResponse.ok(service.getAllLimitsPaged(page, size)));
  }
}
