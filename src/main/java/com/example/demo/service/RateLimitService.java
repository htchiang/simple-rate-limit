package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import com.example.demo.data.RateLimitConstants;
import com.example.demo.data.UsageInfo;
import com.example.demo.mq.RateLimitProducer;
import com.example.demo.persistence.model.RateLimitModel;
import com.example.demo.persistence.repository.RateLimitRepository;
import com.example.demo.redis.RateLimitRedisHelper;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RateLimitService {

  private final RateLimitRepository repository;
  private final RateLimitRedisHelper redisHelper;
  private final RateLimitProducer publisher;
  private final Map<String, RateLimitModel> limitConfigMap = new ConcurrentHashMap<>();

  public RateLimitService(RateLimitRepository repository, RateLimitRedisHelper redisHelper,
      RateLimitProducer publisher) {
    this.repository = repository;
    this.redisHelper = redisHelper;
    this.publisher = publisher;
    preloadLimitsFromDb();
  }

  private void preloadLimitsFromDb() {
    repository.findAll().forEach(limit -> limitConfigMap.put(limit.getApiKey(), limit));
  }

  public CompletableFuture<Boolean> checkAccess(String apiKey) {
    RateLimitModel config = limitConfigMap.get(apiKey);
    if (config == null)
      return CompletableFuture.completedFuture(false);
    return redisHelper.isUnderLimit(RateLimitConstants.buildRedisKey(apiKey), config.getLimit(),
        config.getWindowSeconds());
  }

  public CompletableFuture<UsageInfo> getUsage(String apiKey) {
    RateLimitModel config = limitConfigMap.get(apiKey);
    if (config == null)
      return CompletableFuture.completedFuture(null);
    return redisHelper.getUsage(RateLimitConstants.buildRedisKey(apiKey), config.getLimit());
  }

  public void defineLimit(RateLimitModel limit) {
    limitConfigMap.put(limit.getApiKey(), limit);
    repository.save(limit);
    publisher.broadcast("REFRESH", limit.getApiKey());
  }

  public void deleteLimit(String apiKey) {
    limitConfigMap.remove(apiKey);
    repository.deleteByApiKey(apiKey);
    redisHelper.deleteKey(RateLimitConstants.buildRedisKey(apiKey));
    publisher.broadcast("DELETE", apiKey);
  }

  public void refreshSingle(String apiKey) {
    RateLimitModel limit = repository.findByApiKey(apiKey);
    if (limit != null) {
      limitConfigMap.put(apiKey, limit);
    }
  }

  public void removeLocal(String apiKey) {
    limitConfigMap.remove(apiKey);
    redisHelper.deleteKey(RateLimitConstants.buildRedisKey(apiKey));
  }

  public record PagedResult<T>(List<T> content, int totalElements, int page, int size) {
  }

  public PagedResult<RateLimitModel> getAllLimitsPaged(int page, int size) {
    int offset = page * size;
    List<RateLimitModel> content = repository.findAllPaged(offset, size);
    int total = repository.countAll();
    return new PagedResult<>(content, total, page, size);
  }
}
