package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import com.maxlikarenko.gymcrmsystem.exception.ConflictException;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import com.maxlikarenko.gymcrmsystem.util.PasswordGenerator;
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
    public void activate(String username, boolean activated) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (user.isActive() == activated) {
            log.warn("Activation failed for user {}", username);
            throw new ConflictException("User " + username + " is already " + (activated ? "active" : "inactive"));
        }
        user.setActive(activated);
        log.info("{} user {}", activated ? "Activated" : "Deactivated", username);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        user.setPassword(newPassword);
        log.info("Password changed for user {}", username);
    }

    public void generateCredentials(User user) {
        user.setUsername(generateUsername(user.getFirstName(), user.getLastName()));
        user.setPassword(passwordGenerator.generate());
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
