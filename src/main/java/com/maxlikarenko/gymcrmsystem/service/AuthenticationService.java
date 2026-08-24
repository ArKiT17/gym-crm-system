package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final BruteForceProtectionService bruteForceProtectionService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 BruteForceProtectionService bruteForceProtectionService) {
        this.authenticationManager = authenticationManager;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    public Authentication authenticate(String username, String password) {
        bruteForceProtectionService.check(username);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password)
            );
            bruteForceProtectionService.recordSuccessfulAttempt(username);
            log.debug("User authentication successful for username {}", username);
            return authentication;
        } catch (AuthenticationException exception) {
            bruteForceProtectionService.recordFailedAttempt(username);
            log.warn("Authentication failed for username {}", username);
            throw new UnauthorizedException("Invalid username or password");
        }
    }
}
