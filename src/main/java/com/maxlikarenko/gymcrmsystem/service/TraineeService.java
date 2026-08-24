package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import com.maxlikarenko.gymcrmsystem.account.RegistrationCredentials;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
public class TraineeService {
    private final UserAccountService userAccountService;
    private final TrainerService trainerService;
    private final TraineeRepository traineeRepository;

    public TraineeService(UserAccountService userAccountService,
                          TrainerService trainerService,
                          TraineeRepository traineeRepository) {
        this.userAccountService = userAccountService;
        this.trainerService = trainerService;
        this.traineeRepository = traineeRepository;
    }

    @Transactional
    public RegistrationCredentials create(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        RegistrationCredentials credentials = userAccountService.generateCredentials(trainee.getUser());

        traineeRepository.save(trainee);
        log.info("Creating trainee with username {}", credentials.username());
        return credentials;
    }

    @Transactional
    public Trainee update(String username, String firstName, String lastName,
                          LocalDate dateOfBirth, String address, boolean isActive) {
        log.info("Updating trainee with username {}", username);

        Trainee trainee = get(username);
        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.getUser().setActive(isActive);

        return trainee;
    }

    @Transactional
    public void delete(Long id) {
        Trainee trainee = get(id);
        log.info("Deleting trainee with id {}", id);

        trainee.getTrainers().clear();

        traineeRepository.deleteById(id);
    }

    @Transactional
    public void delete(String username) {
        Trainee trainee = get(username);
        log.info("Deleting trainee with username {}", username);

        trainee.getTrainers().clear();

        traineeRepository.delete(trainee);
    }

    public Trainee get(Long id) {
        log.debug("Finding trainee with id {}", id);
        return traineeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee with id " + id + " not found"));
    }

    public Trainee get(String username) {
        log.debug("Finding trainee with username {}", username);
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee with username " + username + " not found"));
    }

    @Transactional
    public Set<Trainer> updateTrainers(String username, Set<String> trainerUsernames) {
        Trainee trainee = get(username);
        Set<Trainer> newTrainers = trainerService.getActiveByUsernames(trainerUsernames);

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(newTrainers);

        log.info("Updated trainer list for trainee {}", username);
        return trainee.getTrainers();
    }
}
