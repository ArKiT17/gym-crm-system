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

    public AuthenticationService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public Authentication authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password)
            );
            log.debug("User authentication successful for username {}", username);
            return authentication;
        } catch (AuthenticationException exception) {
            log.warn("Authentication failed for username {}", username);
            throw new UnauthorizedException("Invalid username or password");
        }
    }
}
