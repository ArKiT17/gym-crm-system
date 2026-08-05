package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import com.maxlikarenko.gymcrmsystem.exception.ConflictException;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import com.maxlikarenko.gymcrmsystem.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAccountServiceTest {
    private UserRepository userRepository;
    private PasswordGenerator passwordGenerator;
    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordGenerator = mock(PasswordGenerator.class);
        userAccountService = new UserAccountService();
        userAccountService.setUserRepository(userRepository);
        userAccountService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void activateChangesRequestedStatus() {
        User inactiveUser = new User("John", "Smith");
        inactiveUser.setActive(false);
        User activeUser = new User("Jane", "Doe");
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(inactiveUser));
        when(userRepository.findByUsername("Jane.Doe")).thenReturn(Optional.of(activeUser));

        userAccountService.activate("John.Smith", true);
        userAccountService.activate("Jane.Doe", false);

        assertTrue(inactiveUser.isActive());
        assertFalse(activeUser.isActive());
    }

    @Test
    void activateRejectsMissingOrUnchangedStatus() {
        User activeUser = new User("John", "Smith");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(activeUser));

        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> userAccountService.activate("missing", true)),
                () -> assertThrows(ConflictException.class,
                        () -> userAccountService.activate("John.Smith", true))
        );
    }

    @Test
    void changePasswordRejectsMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userAccountService.changePassword("missing", "newPassword"));
    }

    @Test
    void changePasswordUpdatesExistingUser() {
        User user = new User("John", "Smith");
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        userAccountService.changePassword("John.Smith", "newPassword");

        assertEquals("newPassword", user.getPassword());
    }

    @Test
    void generateCredentialsCreatesUniqueUsernameAndPassword() {
        User user = new User("John", "Smith");
        when(userRepository.existsByUsername("John.Smith")).thenReturn(true);
        when(userRepository.existsByUsername("John.Smith1")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Pass123456");

        userAccountService.generateCredentials(user);

        assertAll(
                () -> assertEquals("John.Smith1", user.getUsername()),
                () -> assertEquals("Pass123456", user.getPassword()),
                () -> verify(passwordGenerator).generate()
        );
    }

    @Test
    void generateCredentialsKeepsCheckingUntilUsernameIsFree() {
        User user = new User("John", "Smith");
        when(userRepository.existsByUsername("John.Smith")).thenReturn(true);
        when(userRepository.existsByUsername("John.Smith1")).thenReturn(true);
        when(userRepository.existsByUsername("John.Smith2")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Pass123456");

        userAccountService.generateCredentials(user);

        assertEquals("John.Smith2", user.getUsername());
    }
}
