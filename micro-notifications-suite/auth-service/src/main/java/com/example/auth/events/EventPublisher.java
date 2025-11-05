package com.example.auth.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
  private final RabbitTemplate rabbitTemplate;
  private final String exchange;

  public EventPublisher(RabbitTemplate rabbitTemplate, @Value("${app.rabbit.exchange}") String exchange) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;
  }

  public void publish(String type, String email, String phone, String payload){
    rabbitTemplate.convertAndSend(exchange, type, new AuthEvent(type, email, phone, payload));
  }
}
