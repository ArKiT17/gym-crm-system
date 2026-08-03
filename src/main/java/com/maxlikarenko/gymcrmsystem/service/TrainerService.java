package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainerService {
    private UserAccountService userAccountService;
    private TrainerRepository trainerRepository;

    @Autowired
    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public Trainer create(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        userAccountService.prepareForRegistration(trainer.getUser());

        log.info("Creating trainer with username {}", trainer.getUser().getUsername());
        return trainerRepository.save(trainer);
    }

    @Transactional
    public Trainer update(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }
        userAccountService.validateUser(trainer.getUser());

        log.info("Updating trainer with username {}", trainer.getUser().getUsername());
        return trainerRepository.save(trainer);
    }

    public Optional<Trainer> find(Long id) {
        log.debug("Finding trainer with id {}", id);
        return trainerRepository.findById(id);
    }

    public Optional<Trainer> find(String username) {
        log.debug("Finding trainer with username {}", username);
        return trainerRepository.findByUserUsername(username);
    }

    public Set<Trainer> findAll(Set<String> usernames) {
        log.debug("Finding trainers with usernames {}", usernames);
        Set<Trainer> trainers = trainerRepository.findByUserUsernameIn(usernames);
        log.debug("Found {} trainers", trainers.size());

        if (trainers.size() != usernames.size()) {
            Set<String> foundUsernames = trainers.stream()
                    .map(trainer -> trainer.getUser().getUsername())
                    .collect(Collectors.toSet());

            usernames.stream()
                    .filter(username -> !foundUsernames.contains(username))
                    .findFirst()
                    .ifPresent(missingUsername -> {
                        throw new EntityNotFoundException("Trainer not found: " + missingUsername);
                    });
        }

        return trainers;
    }

    public Set<Trainer> getNotAssignedTrainers(String traineeUsername) {
        log.debug("Getting unassigned trainers for trainee {}", traineeUsername);
        return trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername);
    }
}
