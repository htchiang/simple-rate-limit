package com.example.demo.persistence.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.demo.persistence.model.RateLimitModel;

@Repository
public class RateLimitRepository {

  private final JdbcTemplate jdbcTemplate;

  public RateLimitRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void save(RateLimitModel rateLimit) {
    jdbcTemplate.update("""
            REPLACE INTO rate_limit (api_key, limit_count, window_seconds)
            VALUES (?, ?, ?)
        """, rateLimit.getApiKey(), rateLimit.getLimit(), rateLimit.getWindowSeconds());
  }

  public RateLimitModel findByApiKey(String apiKey) {
    List<RateLimitModel> list = jdbcTemplate.query("""
            SELECT api_key, limit_count, window_seconds
            FROM rate_limit WHERE api_key = ?
        """, new Object[] {apiKey}, (rs, rowNum) -> mapRow(rs));
    return list.isEmpty() ? null : list.get(0);
  }

  public List<RateLimitModel> findAll() {
    return jdbcTemplate.query("""
            SELECT api_key, limit_count, window_seconds
            FROM rate_limit
        """, (rs, rowNum) -> mapRow(rs));
  }

  public void deleteByApiKey(String apiKey) {
    jdbcTemplate.update("DELETE FROM rate_limit WHERE api_key = ?", apiKey);
  }

  public List<RateLimitModel> findAll(int offset, int limit) {
    return jdbcTemplate.query("""
            SELECT api_key, limit_count, window_seconds
            FROM rate_limit
            LIMIT ? OFFSET ?
        """, new Object[] {limit, offset}, (rs, rowNum) -> mapRow(rs));
  }

  public int countAll() {
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rate_limit", Integer.class);
  }

  public List<RateLimitModel> findAllPaged(int offset, int limit) {
    return jdbcTemplate.query("""
          SELECT api_key, limit_count, window_seconds
          FROM rate_limit
          LIMIT ? OFFSET ?
        """, new Object[] {limit, offset}, (rs, rowNum) -> mapRow(rs));
  }

  private RateLimitModel mapRow(ResultSet rs) throws SQLException {
    return new RateLimitModel(rs.getString("api_key"), rs.getInt("limit_count"),
        rs.getInt("window_seconds"));
  }
}
