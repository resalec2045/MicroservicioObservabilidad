package com.example.auth.api;

import com.example.auth.events.EventPublisher;
import com.example.auth.jwt.JwtService;
import com.example.auth.user.User;
import com.example.auth.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final JwtService jwt;
  private final EventPublisher publisher;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public AuthController(UserRepository users, JwtService jwt, EventPublisher publisher) {
    this.users = users; this.jwt = jwt; this.publisher = publisher;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req){
    if(users.findByEmail(req.email()).isPresent()) return ResponseEntity.badRequest().body(Map.of("error","exists"));
    User u = new User(); u.setEmail(req.email()); u.setPasswordHash(encoder.encode(req.password())); u.setPhone(req.phone());
    users.save(u);
    publisher.publish("user.registered", u.getEmail(), u.getPhone(), "{}");
    return ResponseEntity.ok(Map.of("message","registered"));
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req){
    var u = users.findByEmail(req.email()).orElse(null);
    if(u==null || !encoder.matches(req.password(), u.getPasswordHash())){
      return ResponseEntity.status(401).build();
    }
    publisher.publish("user.loggedin", u.getEmail(), u.getPhone(), "{}");
    return ResponseEntity.ok(new TokenResponse(jwt.createToken(u.getEmail())));
  }

  @PostMapping("/password/reset/request")
  public ResponseEntity<?> reset(@Valid @RequestBody PasswordResetRequest req){
    var u = users.findByEmail(req.email()).orElse(null);
    if(u!=null) publisher.publish("user.password.reset.requested", u.getEmail(), u.getPhone(), "{}");
    return ResponseEntity.ok(Map.of("message","if exists, email sent"));
  }

  @PostMapping("/password/update")
  public ResponseEntity<?> update(@RequestHeader("X-User-Email") String email, @Valid @RequestBody PasswordUpdateRequest req){
    var u = users.findByEmail(email).orElse(null);
    if(u==null) return ResponseEntity.status(401).build();
    if(!new BCryptPasswordEncoder().matches(req.oldPassword(), u.getPasswordHash())) return ResponseEntity.status(403).build();
    u.setPasswordHash(new BCryptPasswordEncoder().encode(req.newPassword())); users.save(u);
    publisher.publish("user.password.updated", u.getEmail(), u.getPhone(), "{}");
    return ResponseEntity.ok(Map.of("message","updated"));
  }
}
