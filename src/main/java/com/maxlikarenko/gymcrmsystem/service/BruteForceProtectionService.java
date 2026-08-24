package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.TooManyLoginAttemptsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {
    private final int maxAttempts;
    private final Duration lockoutDuration;
    private final Clock clock;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    @Autowired
    public BruteForceProtectionService(
            @Value("${security.brute-force.max-attempts}") int maxAttempts,
            @Value("${security.brute-force.lockout-duration-seconds}") long lockoutDurationSeconds) {
        this(maxAttempts, Duration.ofSeconds(lockoutDurationSeconds), Clock.systemUTC());
    }

    BruteForceProtectionService(int maxAttempts, Duration lockoutDuration, Clock clock) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        if (!lockoutDuration.isPositive()) {
            throw new IllegalArgumentException("lockoutDuration must be positive");
        }
        this.maxAttempts = maxAttempts;
        this.lockoutDuration = lockoutDuration;
        this.clock = clock;
    }

    public void check(String username) {
        AttemptState state = attempts.get(username);
        if (state == null) {
            return;
        }

        Instant now = clock.instant();
        if (state.lockedUntil() != null && now.isBefore(state.lockedUntil())) {
            throw new TooManyLoginAttemptsException("Too many login attempts. Try again later.");
        }
        if (state.lockedUntil() != null || now.isAfter(state.lastAttempt().plus(lockoutDuration))) {
            attempts.remove(username, state);
        }
    }

    public void recordFailedAttempt(String username) {
        Instant now = clock.instant();
        attempts.compute(username, (key, current) -> {
            if (current == null || now.isAfter(current.lastAttempt().plus(lockoutDuration))) {
                current = new AttemptState(0, now, null);
            }
            int failedAttempts = current.failedAttempts() + 1;
            Instant lockedUntil = failedAttempts >= maxAttempts ? now.plus(lockoutDuration) : null;
            return new AttemptState(failedAttempts, now, lockedUntil);
        });
    }

    public void recordSuccessfulAttempt(String username) {
        attempts.remove(username);
    }

    @Scheduled(fixedDelayString = "${security.brute-force.cleanup-interval-millis}")
    public void cleanupExpiredAttempts() {
        Instant now = clock.instant();
        for (Map.Entry<String, AttemptState> entry : attempts.entrySet()) {
            AttemptState state = entry.getValue();
            if (isExpired(state, now)) {
                attempts.remove(entry.getKey(), state);
            }
        }
    }

    private boolean isExpired(AttemptState state, Instant now) {
        if (state.lockedUntil() != null) {
            return !now.isBefore(state.lockedUntil());
        }
        return now.isAfter(state.lastAttempt().plus(lockoutDuration));
    }

    private record AttemptState(int failedAttempts, Instant lastAttempt, Instant lockedUntil) {
    }
}


