package com.example.notification.service;

import com.example.notification.domain.Notification;
import com.example.notification.domain.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
  private final NotificationRepository repo;
  private final Providers providers;

  public NotificationService(NotificationRepository repo, Providers providers) {
    this.repo = repo; this.providers = providers;
  }

  @Transactional
  public Notification createAndSend(String channel, String email, String phone, String content){
    Notification n = new Notification();
    n.setChannel(channel); n.setRecipientEmail(email); n.setRecipientPhone(phone);
    n.setContent(content); n.setStatus("PENDING");
    repo.save(n);
    try {
      providers.send(n);
      n.setStatus("SENT");
    } catch(Exception e){
      n.setStatus("FAILED"); n.setErrorMessage(e.getMessage());
    }
    return repo.save(n);
  }

  @Transactional
  public Notification schedule(String channel, String email, String phone, String content, Instant when){
    Notification n = new Notification();
    n.setChannel(channel); n.setRecipientEmail(email); n.setRecipientPhone(phone);
    n.setContent(content); n.setStatus("SCHEDULED"); n.setScheduledAt(when);
    return repo.save(n);
  }

  public List<String> channels(){
    return java.util.List.of("email","sms","whatsapp");
  }
}
