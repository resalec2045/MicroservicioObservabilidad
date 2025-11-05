package com.example.auth.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HealthController {

    // `build.properties` (generado por Spring Boot) es opcional
    @Autowired(required = false)
    private BuildProperties buildProperties;

    private Instant startTime() {
        long jvmStartMillis = ManagementFactory.getRuntimeMXBean().getStartTime();
        return Instant.ofEpochMilli(jvmStartMillis);
    }

    private String version() {
        // Toma versión del build; si no hay, revisa variable de entorno o property
        if (buildProperties != null) return buildProperties.getVersion();
        String env = System.getenv().getOrDefault("APP_VERSION", "");
        if (!env.isBlank()) return env;
        return "unknown";
    }

    private String human(Duration d) {
        long days = d.toDaysPart();
        int hours = d.toHoursPart();
        int minutes = d.toMinutesPart();
        int seconds = d.toSecondsPart();
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (days > 0 || hours > 0) sb.append(hours).append("h ");
        if (days > 0 || hours > 0 || minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    private Map<String, Object> baseData(String checkStatus) {
        Instant from = startTime();
        Duration up = Duration.between(from, Instant.now());
        Map<String, Object> data = new HashMap<>();
        data.put("from", from.toString());
        data.put("uptime", human(up));
        data.put("uptimeSeconds", up.toSeconds());
        data.put("version", version());
        data.put("status", checkStatus); // READY o ALIVE
        return data;
    }

    private Map<String, Object> check(String name, String checkStatus) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("status", "UP");
        m.put("data", baseData(checkStatus));
        return m;
    }

    // Agregado: /health -> resumen + checks
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        body.put("checks", List.of(
                check("Readiness check", "READY"),
                check("Liveness check", "ALIVE")
        ));
        return ResponseEntity.ok(body);
    }

    // /health/ready -> solo readiness
    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        body.put("check", check("Readiness check", "READY"));
        return ResponseEntity.ok(body);
    }

    // /health/live -> solo liveness
    @GetMapping("/health/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        body.put("check", check("Liveness check", "ALIVE"));
        return ResponseEntity.ok(body);
    }
}
