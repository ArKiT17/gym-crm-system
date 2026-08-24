package com.maxlikarenko.gymcrmsystem.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class RevokedTokenServiceTest {
    private static final Instant START = Instant.parse("2026-08-21T12:00:00Z");

    private MutableClock clock;
    private RevokedTokenService revokedTokenService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        revokedTokenService = new RevokedTokenService(clock);
    }

    @Test
    void revokesTokenUntilItsExpiration() {
        revokedTokenService.revoke("token-1", START.plusSeconds(60));

        assertTrue(revokedTokenService.isRevoked("token-1"));

        clock.advance(Duration.ofSeconds(59));
        assertTrue(revokedTokenService.isRevoked("token-1"));

        clock.advance(Duration.ofSeconds(1));
        assertFalse(revokedTokenService.isRevoked("token-1"));
    }

    @Test
    void doesNotStoreAlreadyExpiredOrIncompleteRevocation() {
        revokedTokenService.revoke("expired", START.minusSeconds(1));
        revokedTokenService.revoke("at-boundary", START);
        revokedTokenService.revoke(null, START.plusSeconds(60));
        revokedTokenService.revoke("   ", START.plusSeconds(60));
        revokedTokenService.revoke("without-expiration", null);

        assertAll(
                () -> assertFalse(revokedTokenService.isRevoked("expired")),
                () -> assertFalse(revokedTokenService.isRevoked("at-boundary")),
                () -> assertFalse(revokedTokenService.isRevoked(null)),
                () -> assertFalse(revokedTokenService.isRevoked("")),
                () -> assertFalse(revokedTokenService.isRevoked("   ")),
                () -> assertFalse(revokedTokenService.isRevoked("without-expiration"))
        );
    }

    @Test
    void cleanupRemovesOnlyExpiredRevocations() {
        revokedTokenService.revoke("expired", START.plusSeconds(10));
        revokedTokenService.revoke("active", START.plusSeconds(20));

        clock.advance(Duration.ofSeconds(10));
        revokedTokenService.cleanup();

        assertFalse(revokedTokenService.isRevoked("expired"));
        assertTrue(revokedTokenService.isRevoked("active"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}


