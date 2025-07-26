package com.example.demo.redis;

import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;
import com.example.demo.data.RateLimitConstants;
import com.example.demo.data.UsageInfo;
import io.lettuce.core.api.async.RedisAsyncCommands;

@Component
public class RateLimitRedisHelper {

  private final RedisAsyncCommands<String, String> redis;

  public RateLimitRedisHelper(RedisAsyncCommands<String, String> redis) {
    this.redis = redis;
  }

  public CompletableFuture<Boolean> isUnderLimit(String key, int limit, int ttlSeconds) {
    return redis.get(key).toCompletableFuture().thenCompose(val -> {
      boolean isFirstTime = val == null;
      return redis.incr(key).toCompletableFuture().thenCompose(count -> {
        CompletableFuture<Boolean> setTtl =
            isFirstTime ? redis.expire(key, ttlSeconds).toCompletableFuture()
                : CompletableFuture.completedFuture(true);
        return setTtl.thenApply(ok -> count <= limit);
      });
    });
  }

  public CompletableFuture<UsageInfo> getUsage(String key, int limit) {
    CompletableFuture<String> countF = redis.get(key).toCompletableFuture();
    CompletableFuture<Long> ttlF = redis.ttl(key).toCompletableFuture();

    return countF.thenCombine(ttlF, (val, ttl) -> {
      int used = val == null ? 0 : Integer.parseInt(val);
      int remain = Math.max(0, limit - used);

      String prefix = RateLimitConstants.REDIS_KEY_PREFIX;
      String apiKey = key.startsWith(prefix) ? key.substring(prefix.length()) : key;

      return new UsageInfo(apiKey, used, remain, ttl != null ? ttl : -1);
    });
  }

  public CompletableFuture<Boolean> deleteKey(String key) {
    return redis.del(key).toCompletableFuture().thenApply(deleted -> deleted > 0);
  }
}
