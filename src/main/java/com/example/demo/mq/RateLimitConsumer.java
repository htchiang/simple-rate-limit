package com.example.demo.mq;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.demo.data.RateLimitMessageData;
import com.example.demo.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RateLimitConsumer {

  private final RateLimitService service;

  @Value("${rocketmq.name-server}")
  private String nameServer;

  private DefaultMQPushConsumer consumer;

  public RateLimitConsumer(RateLimitService service) {
    this.service = service;
  }

  @PostConstruct
  public void startConsumer() {
    try {
      consumer = new DefaultMQPushConsumer("rate-limit-consumer");
      consumer.setNamesrvAddr(nameServer);
      consumer.subscribe("rate-limit-topic", "*");

      consumer.registerMessageListener(new MessageListenerConcurrently() {
        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
            ConsumeConcurrentlyContext context) {
          for (MessageExt msg : msgs) {
            String payload = new String(msg.getBody(), StandardCharsets.UTF_8);
            log.info("[MQ] Received raw message: {}", payload);
            try {
              RateLimitMessageData data =
                  new ObjectMapper().readValue(payload, RateLimitMessageData.class);
              switch (data.getAction()) {
                case "REFRESH":
                  service.refreshSingle(data.getApiKey());
                  break;
                case "DELETE":
                  service.removeLocal(data.getApiKey());
                  break;
                default:
                  log.warn("Unknown action: {}", data.getAction());
              }
            } catch (Exception e) {
              log.error("Failed to parse or handle MQ message", e);
            }
          }
          return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
      });

      consumer.start();
      log.info("RateLimitConsumer started successfully");
    } catch (Exception e) {
      log.error("Failed to start consumer", e);
    }
  }

  @PreDestroy
  public void shutdown() {
    if (consumer != null) {
      consumer.shutdown();
      log.info("RateLimitConsumer shut down.");
    }
  }
}
