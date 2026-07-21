package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
import com.maxlikarenko.gymcrmsystem.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
public class TraineeService {
    private TraineeRepository traineeRepository;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainee create(Trainee trainee) {
        validateTrainee(trainee);
        trainee.setUsername(generateUsername(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(passwordGenerator.generate());
        log.info("Creating trainee with id {}", trainee.getId());
        return traineeRepository.save(trainee);
    }

    public Trainee update(Trainee trainee) {
        validateTrainee(trainee);
        log.info("Updating trainee with id {}", trainee.getId());
        return traineeRepository.save(trainee);
    }

    public void delete(Long id) {
        log.info("Deleting trainee with id {}", id);
        traineeRepository.deleteById(id);
    }

    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee with id {}", id);
        Optional<Trainee> trainee = traineeRepository.findById(id);
        log.debug("Trainee with id {} found: {}", id, trainee.isPresent());
        return trainee;
    }

    private String generateUsername(String firstName, String lastName) {
        String username = firstName + "." + lastName;
        int suffix = 0;

        while (traineeRepository.existsByUsername(username)) {
            suffix++;
            username = firstName + "." + lastName + suffix;
        }

        return username;
    }

    private void validateTrainee(Trainee trainee) {
        if (trainee == null) {
            log.warn("Cannot process trainee: trainee is null");
            throw new IllegalArgumentException("Trainee cannot be null");
        }
    }
}
