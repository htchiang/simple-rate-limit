
package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import com.example.demo.data.ApiResponse;
import com.example.demo.data.UsageInfo;
import com.example.demo.persistence.model.RateLimitModel;
import com.example.demo.service.RateLimitService;
import com.example.demo.service.RateLimitService.PagedResult;

public class RateLimitControllerTest {

    private RateLimitService mockService;
    private RateLimitController controller;

    @BeforeEach
    void setup() {
        mockService = mock(RateLimitService.class);
        controller = new RateLimitController(mockService);
    }

    @Test
    void testDefineLimit() {
        RateLimitModel limit = new RateLimitModel();
        ResponseEntity<ApiResponse<Void>> response = controller.define(limit);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(mockService).defineLimit(limit);
    }

    @Test
    void testDeleteLimit() {
        String apiKey = "test-key";
        ResponseEntity<ApiResponse<Void>> response = controller.delete(apiKey);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(mockService).deleteLimit(apiKey);
    }

    @Test
    void testCheckAllowed() throws Exception {
        String apiKey = "allowed-key";
        when(mockService.checkAccess(apiKey)).thenReturn(CompletableFuture.completedFuture(true));

        CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> resultFuture = controller.check(apiKey);
        ResponseEntity<ApiResponse<Map<String, Object>>> result = resultFuture.get();

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody().getData().get("allowed")).isEqualTo(true);
    }

    @Test
    void testCheckExceeded() throws Exception {
        String apiKey = "denied-key";
        when(mockService.checkAccess(apiKey)).thenReturn(CompletableFuture.completedFuture(false));
        when(mockService.getUsage(apiKey)).thenReturn(
            CompletableFuture.completedFuture(new UsageInfo(apiKey, 10, 0, 30))
        );

        CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> resultFuture = controller.check(apiKey);
        ResponseEntity<ApiResponse<Map<String, Object>>> result = resultFuture.get();

        assertThat(result.getStatusCodeValue()).isEqualTo(429);
        assertThat(result.getBody().getData().get("allowed")).isEqualTo(false);
    }

    @Test
    void testUsageFound() throws Exception {
        String apiKey = "usage-key";
        UsageInfo usageInfo = new UsageInfo(apiKey, 5, 5, 60);
        when(mockService.getUsage(apiKey)).thenReturn(CompletableFuture.completedFuture(usageInfo));

        CompletableFuture<ResponseEntity<ApiResponse<UsageInfo>>> resultFuture = controller.usage(apiKey);
        ResponseEntity<ApiResponse<UsageInfo>> result = resultFuture.get();

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody().getData().getApiKey()).isEqualTo(apiKey);
    }

    @Test
    void testUsageNotFound() throws Exception {
        String apiKey = "missing-key";
        when(mockService.getUsage(apiKey)).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<ResponseEntity<ApiResponse<UsageInfo>>> resultFuture = controller.usage(apiKey);
        ResponseEntity<ApiResponse<UsageInfo>> result = resultFuture.get();

        assertThat(result.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void testGetAllLimits() {
        PagedResult<RateLimitModel> page = new PagedResult<>(List.of(), 0, 0, 10);
        when(mockService.getAllLimitsPaged(0, 10)).thenReturn(page);

        ResponseEntity<ApiResponse<PagedResult<RateLimitModel>>> result = controller.getAllLimits(0, 10);
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody().getData()).isEqualTo(page);
    }
}
