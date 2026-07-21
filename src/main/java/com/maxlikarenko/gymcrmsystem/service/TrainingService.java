package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TrainingService {
    private final TrainingRepository trainingRepository;

    @Autowired
    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public Training create(Training training) {
        validateTraining(training);
        log.info("Creating training with id {}", training.getId());
        return trainingRepository.save(training);
    }

    public Optional<Training> findById(Long id) {
        log.debug("Finding training with id {}", id);
        Optional<Training> training = trainingRepository.findById(id);
        log.debug("Training with id {} found: {}", id, training.isPresent());
        return training;
    }

    private void validateTraining(Training training) {
        if (training == null) {
            log.warn("Cannot process training: training is null");
            throw new IllegalArgumentException("Training cannot be null");
        }
    }
}
