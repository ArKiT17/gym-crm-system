package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import com.maxlikarenko.gymcrmsystem.util.PasswordGenerator;
import jakarta.persistence.EntityNotFoundException;
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

    // validateUser

    @Test
    void validateUserThrowsForNullUser() {
        assertThrows(IllegalArgumentException.class, () -> userAccountService.validateUser(null));
    }

    @Test
    void validateUserThrowsForNullFirstName() {
        User user = new User(null, "Smith", "u", "p", true);
        assertThrows(IllegalArgumentException.class, () -> userAccountService.validateUser(user));
    }

    @Test
    void validateUserThrowsForBlankFirstName() {
        User user = new User("  ", "Smith", "u", "p", true);
        assertThrows(IllegalArgumentException.class, () -> userAccountService.validateUser(user));
    }

    @Test
    void validateUserThrowsForNullLastName() {
        User user = new User("John", null, "u", "p", true);
        assertThrows(IllegalArgumentException.class, () -> userAccountService.validateUser(user));
    }

    @Test
    void validateUserThrowsForBlankLastName() {
        User user = new User("John", " ", "u", "p", true);
        assertThrows(IllegalArgumentException.class, () -> userAccountService.validateUser(user));
    }

    @Test
    void validateUserPassesForValidUser() {
        User user = new User("John", "Smith", "u", "p", true);
        assertDoesNotThrow(() -> userAccountService.validateUser(user));
    }

    // prepareForRegistration

    @Test
    void prepareForRegistrationThrowsForNullUser() {
        assertThrows(IllegalArgumentException.class,
                () -> userAccountService.prepareForRegistration(null));
        verifyNoInteractions(userRepository, passwordGenerator);
    }

    @Test
    void prepareForRegistrationSetsUsernameAndPassword() {
        User user = new User("John", "Smith", null, null, false);
        when(userRepository.existsByUsername("John.Smith")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Pass123456");

        userAccountService.prepareForRegistration(user);

        assertEquals("John.Smith", user.getUsername());
        assertEquals("Pass123456", user.getPassword());
    }

    @Test
    void prepareForRegistrationAddsSuffixOnFirstCollision() {
        User user = new User("John", "Smith", null, null, false);
        when(userRepository.existsByUsername("John.Smith")).thenReturn(true);
        when(userRepository.existsByUsername("John.Smith1")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Pass123456");

        userAccountService.prepareForRegistration(user);

        assertEquals("John.Smith1", user.getUsername());
    }

    @Test
    void prepareForRegistrationKeepsIncrementingUntilFreeUsernameFound() {
        User user = new User("John", "Smith", null, null, false);
        when(userRepository.existsByUsername("John.Smith")).thenReturn(true);
        when(userRepository.existsByUsername("John.Smith1")).thenReturn(true);
        when(userRepository.existsByUsername("John.Smith2")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Pass123456");

        userAccountService.prepareForRegistration(user);

        assertEquals("John.Smith2", user.getUsername());
    }

    // activate

    @Test
    void activateThrowsEntityNotFoundWhenUserMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userAccountService.activate("missing"));
    }

    @Test
    void activateThrowsIllegalStateWhenUserAlreadyActive() {
        User user = new User("John", "Smith", "John.Smith", "pass", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class, () -> userAccountService.activate("John.Smith"));
    }

    @Test
    void activateSetsActiveTrueForInactiveUser() {
        User user = new User("John", "Smith", "John.Smith", "pass", false);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        userAccountService.activate("John.Smith");

        assertTrue(user.isActive());
    }

    // deactivate

    @Test
    void deactivateThrowsEntityNotFoundWhenUserMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userAccountService.deactivate("missing"));
    }

    @Test
    void deactivateThrowsIllegalStateWhenUserAlreadyInactive() {
        User user = new User("John", "Smith", "John.Smith", "pass", false);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class, () -> userAccountService.deactivate("John.Smith"));
    }

    @Test
    void deactivateSetsActiveFalseForActiveUser() {
        User user = new User("John", "Smith", "John.Smith", "pass", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        userAccountService.deactivate("John.Smith");

        assertFalse(user.isActive());
    }

    // changePassword

    @Test
    void changePasswordThrowsEntityNotFoundWhenUserMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> userAccountService.changePassword("missing", "new"));
    }

    @Test
    void changePasswordThrowsForNullNewPassword() {
        User user = new User("John", "Smith", "John.Smith", "oldPass", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class,
                () -> userAccountService.changePassword("John.Smith", null));
    }

    @Test
    void changePasswordThrowsForBlankNewPassword() {
        User user = new User("John", "Smith", "John.Smith", "oldPass", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class,
                () -> userAccountService.changePassword("John.Smith", "   "));
    }

    @Test
    void changePasswordUpdatesUserPassword() {
        User user = new User("John", "Smith", "John.Smith", "oldPass", true);
        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        userAccountService.changePassword("John.Smith", "newPass123");

        assertEquals("newPass123", user.getPassword());
    }
}
