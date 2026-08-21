package com.maxlikarenko.gymcrmsystem.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RevokedTokenService {
    private final Clock clock;
    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public RevokedTokenService() {
        this(Clock.systemUTC());
    }

    RevokedTokenService(Clock clock) {
        this.clock = clock;
    }

    public void revoke(String tokenId, Instant expiresAt) {
        if (tokenId == null || tokenId.isBlank() || expiresAt == null) {
            return;
        }

        if (expiresAt.isAfter(clock.instant())) {
            revokedTokens.put(tokenId, expiresAt);
        }
    }

    public boolean isRevoked(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }

        Instant expiresAt = revokedTokens.get(tokenId);
        if (expiresAt == null) {
            return false;
        }

        if (!expiresAt.isAfter(clock.instant())) {
            revokedTokens.remove(tokenId, expiresAt);
            return false;
        }

        return true;
    }

    @Scheduled(fixedDelayString = "${security.jwt.revoked-token-cleanup-interval-millis}")
    public void cleanup() {
        Instant now = clock.instant();
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}

