package com.debugflow.core.web;

import com.debugflow.core.session.SessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private final SessionManager sessionManager;
    private final int defaultTtlMinutes;

    public SessionController(SessionManager sessionManager, int defaultTtlMinutes) {
        this.sessionManager = sessionManager;
        this.defaultTtlMinutes = defaultTtlMinutes;
    }

    @GetMapping
    public Map<String, Object> get() {
        Map<String, Object> out = new HashMap<>();
        out.put("active", sessionManager.isActive());
        sessionManager.ttlRemaining().ifPresentOrElse(d -> out.put("ttlSeconds", d.toSeconds()),
                () -> out.put("ttlSeconds", 0));
        return out;
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable(@RequestBody(required = false) Map<String, Object> body) {
        int ttl = defaultTtlMinutes;
        if (body != null && body.get("ttlMinutes") instanceof Number n) {
            ttl = n.intValue();
        }
        sessionManager.enable(Duration.ofMinutes(ttl));
        return ResponseEntity.ok(get());
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable() {
        sessionManager.disable();
        return ResponseEntity.ok(get());
    }
}

