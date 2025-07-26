package com.example.demo.mq;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Component;
import com.example.demo.data.RateLimitMessageData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RateLimitProducer {
  private final DefaultMQProducer mqProducer;
  private final ExecutorService executor = Executors.newFixedThreadPool(2);
  private final ObjectMapper mapper = new ObjectMapper();

  public RateLimitProducer(DefaultMQProducer mqProducer) {
    this.mqProducer = mqProducer;
  }

  public void broadcast(String action, String apiKey) {
    executor.submit(() -> {
      try {
        String json = mapper.writeValueAsString(new RateLimitMessageData(action, apiKey));
        mqProducer.send(new Message("rate-limit-topic", json.getBytes(StandardCharsets.UTF_8)));
        log.info("Broadcasted MQ message: {}", json);
      } catch (Exception e) {
        log.warn("Failed to send MQ message", e);
      }
    });
  }
}
