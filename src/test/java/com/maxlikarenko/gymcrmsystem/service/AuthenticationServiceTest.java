package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private AuthenticationManager authenticationManager;
    private BruteForceProtectionService bruteForceProtectionService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        bruteForceProtectionService = mock(BruteForceProtectionService.class);
        authenticationService = new AuthenticationService(authenticationManager, bruteForceProtectionService);
    }

    @Test
    void authenticateThrowsWhenUserNotFound() {
        doThrow(new BadCredentialsException("Invalid username or password"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        assertThrows(UnauthorizedException.class,
                () -> authenticationService.authenticate("missing", "anyPass"));
        verify(bruteForceProtectionService).recordFailedAttempt("missing");
    }

    @Test
    void authenticateThrowsWhenPasswordIsWrong() {
        doThrow(new BadCredentialsException("Invalid username or password"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authenticationService.authenticate("John.Smith", "wrongPass"));
        assertEquals("Invalid username or password", exception.getMessage());
        verify(bruteForceProtectionService).recordFailedAttempt("John.Smith");
    }

    @Test
    void authenticateReturnsAuthenticationOnSuccess() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        assertSame(authentication, authenticationService.authenticate("John.Smith", "correctPass"));
        verify(bruteForceProtectionService).recordSuccessfulAttempt("John.Smith");
    }

    @Test
    void authenticateUsesExactPasswordMatch() {
        doThrow(new BadCredentialsException("Invalid username or password"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        assertThrows(UnauthorizedException.class,
                () -> authenticationService.authenticate("John.Smith", "pass1"));
    }

    @Test
    void authenticateRejectsInactiveUser() {
        doThrow(new BadCredentialsException("User is disabled"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        assertThrows(UnauthorizedException.class,
                () -> authenticationService.authenticate("John.Smith", "correctPass"));
    }
}
