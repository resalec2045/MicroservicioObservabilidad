package com.example.orchestrator.listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEventsListener {
  private final RabbitTemplate rabbitTemplate;
  private final String exchange;

  public AuthEventsListener(RabbitTemplate rabbitTemplate, @Value("${app.rabbit.exchange}") String exchange) {
    this.rabbitTemplate = rabbitTemplate;
    this.exchange = exchange;
  }

  @RabbitListener(queues = "auth.user.registered")
  public void onRegistered(Map<String, Object> evt){
    // Regla: al crear cuenta, enviar correo de confirmación.
    String email = (String) evt.get("email");
    Map<String,Object> notify = new HashMap<>();
    notify.put("channel","email");
    notify.put("email", email);
    notify.put("content","Bienvenido. Confirma tu email usando este link: https://example.com/confirm?email="+email);
    rabbitTemplate.convertAndSend(exchange, "notify.request", notify);
  }

  @RabbitListener(queues = "auth.user.loggedin")
  public void onLogin(Map<String, Object> evt){
    // Regla: seguridad: email y sms
    String email = (String) evt.get("email");
    String phone = (String) evt.get("phone");
    for(String channel : new String[]{"email","sms"}){
      Map<String,Object> notify = new HashMap<>();
      notify.put("channel", channel);
      notify.put("email", email);
      notify.put("phone", phone);
      notify.put("content","Alerta de seguridad: nuevo ingreso en tu cuenta.");
      rabbitTemplate.convertAndSend(exchange, "notify.request", notify);
    }
  }

  @RabbitListener(queues = "auth.user.password.reset.requested")
  public void onReset(Map<String, Object> evt){
    String email = (String) evt.get("email");
    Map<String,Object> notify = new HashMap<>();
    notify.put("channel","email");
    notify.put("email", email);
    notify.put("content","Recuperación de clave: usa este link: https://example.com/reset?email="+email);
    rabbitTemplate.convertAndSend(exchange, "notify.request", notify);
  }

  @RabbitListener(queues = "auth.user.password.updated")
  public void onUpdated(Map<String, Object> evt){
    String email = (String) evt.get("email");
    String phone = (String) evt.get("phone");
    for(String channel : new String[]{"email","sms"}){
      Map<String,Object> notify = new HashMap<>();
      notify.put("channel", channel);
      notify.put("email", email);
      notify.put("phone", phone);
      notify.put("content","Tu contraseña fue actualizada. Si no fuiste tú, contacta soporte.");
      rabbitTemplate.convertAndSend(exchange, "notify.request", notify);
    }
  }
}
