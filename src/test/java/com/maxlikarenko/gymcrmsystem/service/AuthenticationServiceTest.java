package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private UserRepository userRepository;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationService = new AuthenticationService();
        authenticationService.setUserRepository(userRepository);
    }

    @Test
    void authenticateThrowsWhenUserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("missing", "anyPass"));
    }

    @Test
    void authenticateThrowsWhenPasswordIsWrong() {
        User user = new User("John", "Smith", "John.Smith", "correctPass", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("John.Smith", "wrongPass"));
    }

    @Test
    void authenticateReturnsTrueOnSuccess() {
        User user = new User("John", "Smith", "John.Smith", "correctPass", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertTrue(authenticationService.authenticate("John.Smith", "correctPass"));
    }

    @Test
    void authenticateUsesExactPasswordMatch() {
        User user = new User("John", "Smith", "John.Smith", "Pass1", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("John.Smith", "pass1"));
    }
}
