package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.repository.TrainerRepository;
import com.maxlikarenko.gymcrmsystem.util.PasswordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TrainerService {
    private TrainerRepository trainerRepository;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer create(Trainer trainer) {
        validateTrainer(trainer);
        trainer.setUsername(generateUsername(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(passwordGenerator.generate());
        log.info("Creating trainer with id {}", trainer.getId());
        return trainerRepository.save(trainer);
    }

    public Trainer update(Trainer trainer) {
        validateTrainer(trainer);
        log.info("Updating trainer with id {}", trainer.getId());
        return trainerRepository.save(trainer);
    }

    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer with id {}", id);
        Optional<Trainer> trainer = trainerRepository.findById(id);
        log.debug("Trainer with id {} found: {}", id, trainer.isPresent());
        return trainer;
    }

    private String generateUsername(String firstName, String lastName) {
        String username = firstName + "." + lastName;
        int suffix = 0;

        while (trainerRepository.existsByUsername(username)) {
            suffix++;
            username = firstName + "." + lastName + suffix;
        }

        return username;
    }

    private void validateTrainer(Trainer trainer) {
        if (trainer == null) {
            log.warn("Cannot process trainer: trainer is null");
            throw new IllegalArgumentException("Trainer cannot be null");
        }
    }
}
