package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.UnauthorizedException;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        if (!user.getPassword().equals(password)) {
            log.warn("Authentication failed for user {}", username);
            throw new UnauthorizedException("Invalid username or password");
        }
        log.debug("User authentication successful for username {}", username);
        return true;
    }
}
