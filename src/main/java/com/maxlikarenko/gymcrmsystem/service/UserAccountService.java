package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import com.maxlikarenko.gymcrmsystem.util.PasswordGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserAccountService {
    private UserRepository userRepository;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Transactional
    public void activate(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        if (user.isActive()) {
            log.warn("Activation failed for user {}", username);
            throw new IllegalStateException("User " + username + " is already active");
        }
        user.setActive(true);
        log.info("Activated user {}", username);
    }

    @Transactional
    public void deactivate(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        if (!user.isActive()) {
            log.warn("Deactivation failed for user {}", username);
            throw new IllegalStateException("User " + username + " is already inactive");
        }
        user.setActive(false);
        log.info("Deactivated user {}", username);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        if (isBlank(newPassword)) {
            log.warn("Password change failed for user {}: new password is blank", username);
            throw new IllegalArgumentException("New password cannot be blank");
        }
        user.setPassword(newPassword);
        log.info("Password changed for user {}", username);
    }

    public void prepareForRegistration(User user) {
        validateUser(user);
        user.setUsername(generateUsername(user.getFirstName(), user.getLastName()));
        user.setPassword(passwordGenerator.generate());
    }

    public void validateUser(User user) {
        if (user == null) {
            log.warn("Cannot process user: user is null");
            throw new IllegalArgumentException("User cannot be null");
        }
        if (isBlank(user.getFirstName()) || isBlank(user.getLastName())) {
            log.warn("Cannot process user: first name and last name are required");
            throw new IllegalArgumentException("User first name and last name are required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String generateUsername(String firstName, String lastName) {
        String username = firstName + "." + lastName;
        int suffix = 0;
        String candidate = username;

        while (userRepository.existsByUsername(candidate)) {
            suffix++;
            candidate = username + suffix;
        }

        return candidate;
    }
}
