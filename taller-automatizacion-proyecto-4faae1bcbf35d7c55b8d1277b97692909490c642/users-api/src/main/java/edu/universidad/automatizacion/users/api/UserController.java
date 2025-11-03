package edu.universidad.automatizacion.users.api;

import edu.universidad.automatizacion.users.app.UserService;
import edu.universidad.automatizacion.users.domain.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserDTO dto) {
        logger.info("Creating user: {}", dto);
        UserDTO saved = service.create(dto);
        logger.info("User created with id: {}", saved.id());
        return ResponseEntity.created(URI.create("/users/" + saved.id())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") String id) {
        logger.info("Fetching user with id: {}", id);
        return service.findOptional(id)
                .<ResponseEntity<?>>map(user -> {
                    logger.info("User found: {}", user);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    logger.warn("User not found: {}", id);
                    return ResponseEntity.status(404).body(java.util.Map.of(
                        "message", "User not found",
                        "id", id
                    ));
                });
    }

    @GetMapping
    public List<UserDTO> list() {
        logger.info("Listing all users");
        return service.list();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable("id") String id, @Valid @RequestBody UserDTO dto) {
        logger.info("Updating user with id: {}", id);
        return service.findOptional(id)
                .map(existing -> {
                    logger.info("User exists, updating: {}", id);
                    return ResponseEntity.ok(service.update(id, dto));
                })
                .orElseGet(() -> {
                    logger.warn("User not found for update: {}", id);
                    return ResponseEntity.status(404).body((UserDTO) null);
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        logger.info("Deleting user with id: {}", id);
        boolean deleted = service.delete(id);
        if (deleted) {
            logger.info("User deleted: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("User not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
