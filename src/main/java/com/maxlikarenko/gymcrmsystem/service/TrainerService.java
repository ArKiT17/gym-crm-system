package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import com.maxlikarenko.gymcrmsystem.account.RegistrationCredentials;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainerService {
    private final UserAccountService userAccountService;
    private final TrainerRepository trainerRepository;

    public TrainerService(UserAccountService userAccountService,
                          TrainerRepository trainerRepository) {
        this.userAccountService = userAccountService;
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public RegistrationCredentials create(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        RegistrationCredentials credentials = userAccountService.generateCredentials(trainer.getUser());

        trainerRepository.save(trainer);
        log.info("Creating trainer with username {}", credentials.username());
        return credentials;
    }

    @Transactional
    public Trainer update(String username, String firstName, String lastName, boolean isActive) {
        log.info("Updating trainer with username {}", username);

        Trainer trainer = get(username);
        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);
        trainer.getUser().setActive(isActive);

        return trainer;
    }

    public Trainer get(Long id) {
        log.debug("Finding trainer with id {}", id);
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer with id " + id + " not found"));
    }

    public Trainer get(String username) {
        log.debug("Finding trainer with username {}", username);
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer with username " + username + " not found"));
    }

    public Set<Trainer> getActiveByUsernames(Set<String> usernames) {
        log.debug("Finding trainers with usernames {}", usernames);
        Set<Trainer> trainers = trainerRepository.findByUserActiveTrueAndUserUsernameIn(usernames);
        log.debug("Found {} trainers", trainers.size());

        if (trainers.size() != usernames.size()) {
            Set<String> foundUsernames = trainers.stream()
                    .map(trainer -> trainer.getUser().getUsername())
                    .collect(Collectors.toSet());

            usernames.stream()
                    .filter(username -> !foundUsernames.contains(username))
                    .findFirst()
                    .ifPresent(missingUsername -> {
                        throw new ResourceNotFoundException("Trainer with username " + missingUsername + " not found");
                    });
        }

        return trainers;
    }

    public Set<Trainer> getNotAssignedTrainers(String traineeUsername) {
        log.debug("Getting unassigned trainers for trainee {}", traineeUsername);
        return trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername);
    }
}
