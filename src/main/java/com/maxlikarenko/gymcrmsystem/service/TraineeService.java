package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class TraineeService {
    private UserAccountService userAccountService;
    private TrainerService trainerService;
    private TraineeRepository traineeRepository;

    @Autowired
    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Transactional
    public Trainee create(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        userAccountService.prepareForRegistration(trainee.getUser());

        log.info("Creating trainee with username {}", trainee.getUser().getUsername());
        return traineeRepository.save(trainee);
    }

    @Transactional
    public Trainee update(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }
        userAccountService.validateUser(trainee.getUser());

        log.info("Updating trainee with username {}", trainee.getUser().getUsername());
        return traineeRepository.save(trainee);
    }

    @Transactional
    public void delete(Long id) {
        Trainee trainee = find(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with id " + id + " not found"));
        log.info("Deleting trainee with id {}", id);

        trainee.getTrainers().clear();

        traineeRepository.deleteById(id);
    }

    @Transactional
    public void delete(String username) {
        Trainee trainee = find(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
        log.info("Deleting trainee with username {}", username);

        trainee.getTrainers().clear();

        traineeRepository.delete(trainee);
    }

    public Optional<Trainee> find(Long id) {
        log.debug("Finding trainee with id {}", id);
        return traineeRepository.findById(id);
    }

    public Optional<Trainee> find(String username) {
        log.debug("Finding trainee with username {}", username);
        return traineeRepository.findByUserUsername(username);
    }

    @Transactional
    public Set<Trainer> updateTrainers(String username, Set<String> trainerUsernames) {
        Trainee trainee = find(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
        Set<Trainer> newTrainers = trainerService.findAll(trainerUsernames);

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(newTrainers);

        log.info("Updated trainer list for trainee {}", username);
        return trainee.getTrainers();
    }
}
