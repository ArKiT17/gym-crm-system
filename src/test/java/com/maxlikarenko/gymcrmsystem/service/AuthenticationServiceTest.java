package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        User user = user("John", "Smith", "John.Smith", "correctPass");
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("John.Smith", "wrongPass"));
    }

    @Test
    void authenticateReturnsTrueOnSuccess() {
        User user = user("John", "Smith", "John.Smith", "correctPass");
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertTrue(authenticationService.authenticate("John.Smith", "correctPass"));
    }

    @Test
    void authenticateUsesExactPasswordMatch() {
        User user = user("John", "Smith", "John.Smith", "Pass1");
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("John.Smith", "pass1"));
    }

    private User user(String firstName, String lastName, String username, String password) {
        User user = new User(firstName, lastName);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}
