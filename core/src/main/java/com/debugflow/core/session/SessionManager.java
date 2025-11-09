package com.debugflow.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "debugflow-session-scheduler");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean active;
    private volatile Instant expiresAt;
    private volatile ScheduledFuture<?> expiryTask;

    public synchronized void enable(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            enableInfinite();
            return;
        }
        this.active = true;
        this.expiresAt = Instant.now().plus(ttl);
        if (expiryTask != null) expiryTask.cancel(false);
        expiryTask = scheduler.schedule(this::disable, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public synchronized void enableInfinite() {
        this.active = true;
        this.expiresAt = null;
        if (expiryTask != null) {
            expiryTask.cancel(false);
            expiryTask = null;
        }
    }

    public synchronized void disable() {
        this.active = false;
        this.expiresAt = null;
        if (expiryTask != null) {
            expiryTask.cancel(false);
            expiryTask = null;
        }
    }

    public boolean isActive() {
        if (!active) return false;
        Instant exp = this.expiresAt;
        if (exp != null && Instant.now().isAfter(exp)) {
            // Expired; deactivate lazily.
            disable();
            return false;
        }
        return true;
    }

    public Optional<Duration> ttlRemaining() {
        Instant exp = this.expiresAt;
        if (!active || exp == null) return Optional.empty();
        Duration d = Duration.between(Instant.now(), exp);
        if (d.isNegative()) return Optional.empty();
        return Optional.of(d);
    }
}
