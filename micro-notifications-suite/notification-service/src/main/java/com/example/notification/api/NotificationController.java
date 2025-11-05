package com.example.notification.api;

import com.example.notification.domain.Notification;
import com.example.notification.domain.NotificationRepository;
import com.example.notification.service.NotificationService;
import com.example.notification.service.TemplateEngine;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {
  private final NotificationService service;
  private final TemplateEngine tmpl;
  private final NotificationRepository repo;

  public NotificationController(NotificationService service, TemplateEngine tmpl, NotificationRepository repo) {
    this.service = service; this.tmpl = tmpl; this.repo = repo;
  }

  @GetMapping("/channels")
  public java.util.List<String> channels(){ return service.channels(); }

  @PostMapping("/notifications")
  public ResponseEntity<?> send(@Valid @RequestBody SendRequest req){
    var responses = new java.util.ArrayList<NotificationResponse>();
    for(String c: req.channels()){
      String content = tmpl.render(req.template(), req.variables());
      Notification n = service.createAndSend(c, req.recipientEmail(), req.recipientPhone(), content);
      responses.add(new NotificationResponse(n.getId(), n.getStatus()));
    }
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/notifications/schedule")
  public ResponseEntity<?> schedule(@Valid @RequestBody ScheduleRequest req){
    if(req.sendAt().isBefore(Instant.now())) return ResponseEntity.badRequest().body(Map.of("error","sendAt must be future"));
    var responses = new java.util.ArrayList<NotificationResponse>();
    for(String c: req.channels()){
      String content = tmpl.render(req.template(), req.variables());
      Notification n = service.schedule(c, req.recipientEmail(), req.recipientPhone(), content, req.sendAt());
      responses.add(new NotificationResponse(n.getId(), n.getStatus()));
    }
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/notifications/{id}")
  public ResponseEntity<?> status(@PathVariable String id){
    return repo.findById(id).<ResponseEntity<?>>map(n->ResponseEntity.ok(Map.of(
        "id", n.getId() != null ? n.getId() : "",
        "status", n.getStatus() != null ? n.getStatus() : "",
        "channel", n.getChannel() != null ? n.getChannel() : "",
        "scheduledAt", n.getScheduledAt() != null ? n.getScheduledAt() : "",
        "createdAt", n.getCreatedAt() != null ? n.getCreatedAt() : "",
        "error", n.getErrorMessage() != null ? n.getErrorMessage() : ""
    ))).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/notifications")
  public ResponseEntity<?> list(@RequestParam(defaultValue = "") String status,
                                @RequestParam(defaultValue = "") String channel,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size){
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    var pageObj = repo.findByStatusContainingIgnoreCaseAndChannelContainingIgnoreCase(status, channel, pageable);
    var map = new HashMap<String,Object>();
    map.put("content", pageObj.getContent());
    map.put("page", pageObj.getNumber());
    map.put("size", pageObj.getSize());
    map.put("totalElements", pageObj.getTotalElements());
    map.put("totalPages", pageObj.getTotalPages());
    return ResponseEntity.ok(map);
  }
}
