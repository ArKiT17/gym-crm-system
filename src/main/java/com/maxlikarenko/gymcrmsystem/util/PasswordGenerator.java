package com.maxlikarenko.gymcrmsystem.util;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
@Component
public final class PasswordGenerator {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        log.debug("Generating password with length {}", PASSWORD_LENGTH);
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}
