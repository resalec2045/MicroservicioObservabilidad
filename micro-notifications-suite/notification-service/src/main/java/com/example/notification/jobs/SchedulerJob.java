package com.example.notification.jobs;

import com.example.notification.domain.Notification;
import com.example.notification.domain.NotificationRepository;
import com.example.notification.service.Providers;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class SchedulerJob {
  private final NotificationRepository repo;
  private final Providers providers;

  public SchedulerJob(NotificationRepository repo, Providers providers) { this.repo = repo; this.providers = providers; }

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void dispatchScheduled(){
    List<Notification> due = repo.findAll().stream()
        .filter(n -> "SCHEDULED".equals(n.getStatus()) && n.getScheduledAt()!=null && n.getScheduledAt().isBefore(Instant.now()))
        .toList();
    for(Notification n: due){
      try{
        providers.send(n);
        n.setStatus("SENT");
      }catch(Exception e){
        n.setStatus("FAILED"); n.setErrorMessage(e.getMessage());
      }
      repo.save(n);
    }
  }
}
