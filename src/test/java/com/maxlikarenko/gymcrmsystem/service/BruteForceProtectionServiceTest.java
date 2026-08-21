package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BruteForceProtectionServiceTest {
    private static final Instant START = Instant.parse("2026-08-21T12:00:00Z");
    private final MutableClock clock = new MutableClock(START);
    private final BruteForceProtectionService protectionService =
            new BruteForceProtectionService(3, Duration.ofMinutes(5), clock);

    @Test
    void allowsLoginBeforeMaximumFailedAttempts() {
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");

        assertDoesNotThrow(() -> protectionService.check("John.Smith"));
    }

    @Test
    void blocksLoginAfterMaximumFailedAttempts() {
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");

        assertThrows(TooManyLoginAttemptsException.class,
                () -> protectionService.check("John.Smith"));
    }

    @Test
    void successfulLoginResetsFailedAttempts() {
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordSuccessfulAttempt("John.Smith");

        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");
        assertDoesNotThrow(() -> protectionService.check("John.Smith"));
    }

    @Test
    void allowsLoginAfterLockoutExpires() {
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");

        clock.advance(Duration.ofMinutes(5));

        assertDoesNotThrow(() -> protectionService.check("John.Smith"));
    }

    @Test
    void tracksAttemptsPerUsername() {
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("John.Smith");
        protectionService.recordFailedAttempt("Jane.Doe");

        assertDoesNotThrow(() -> protectionService.check("Jane.Doe"));
        assertDoesNotThrow(() -> protectionService.check("John.Smith"));
    }

    @Test
    void cleanupRemovesExpiredAttemptStates() throws Exception {
        protectionService.recordFailedAttempt("Expired.Unlocked");
        protectionService.recordFailedAttempt("Expired.Locked");
        protectionService.recordFailedAttempt("Expired.Locked");
        protectionService.recordFailedAttempt("Expired.Locked");

        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        protectionService.cleanupExpiredAttempts();

        assertEquals(0, attempts().size());
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> attempts() throws Exception {
        Field field = BruteForceProtectionService.class.getDeclaredField("attempts");
        field.setAccessible(true);
        return (Map<String, ?>) field.get(protectionService);
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

