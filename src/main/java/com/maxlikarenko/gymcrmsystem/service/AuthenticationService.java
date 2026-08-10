package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.UnauthorizedException;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        checkPassword(user, password);
        checkStatus(user);

        log.debug("User authentication successful for username {}", username);
        return true;
    }

    private void checkPassword(User user, String password) {
        if (!user.getPassword().equals(password)) {
            log.warn("Authentication failed for user {}", user.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    private void checkStatus(User user) {
        if (!user.isActive()) {
            log.warn("Authentication failed. User {} is inactive", user.getUsername());
            throw new UnauthorizedException("Account is inactive");
        }
    }
}
