package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestMQController {
  private final DefaultMQProducer producer;

  public TestMQController(DefaultMQProducer producer) {
    this.producer = producer;
  }

  @GetMapping("/mq")
  public String sendTest() throws Exception {
    String body = "{\"action\":\"REFRESH\",\"apiKey\":\"test-key\"}";
    Message msg = new Message("rate-limit-topic", body.getBytes(StandardCharsets.UTF_8));
    SendResult result = producer.send(msg);
    return "Send Result: " + result;
  }
}
