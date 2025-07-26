package com.example.demo.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

  @Value("${rocketmq.name-server}")
  private String nameServer;
  
  @Bean
  public DefaultMQProducer mqProducer() throws Exception {
    DefaultMQProducer producer = new DefaultMQProducer("rate-limit-producer-group");
    producer.setNamesrvAddr(nameServer);
    producer.start();
    return producer;
  }
}
