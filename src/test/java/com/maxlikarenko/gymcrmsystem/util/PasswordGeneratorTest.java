package com.maxlikarenko.gymcrmsystem.util;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generateReturnsPasswordWithTenCharacters() {
        String password = passwordGenerator.generate();

        assertEquals(10, password.length());
    }

    @Test
    void generateReturnsPasswordContainingOnlyAllowedCharacters() {
        String password = passwordGenerator.generate();

        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }

    @Test
    void generateProducesDifferentPasswords() {
        Set<String> passwords = IntStream.range(0, 100)
                .mapToObj(index -> passwordGenerator.generate())
                .collect(Collectors.toSet());

        assertNotEquals(1, passwords.size());
    }
}
