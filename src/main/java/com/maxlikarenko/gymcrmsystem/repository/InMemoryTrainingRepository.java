package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InMemoryTrainingRepository implements TrainingRepository {

    private final Map<Long, Training> storage;

    public InMemoryTrainingRepository(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = storage;
    }

    @Override
    public Training save(Training training) {
        if (training == null) {
            log.warn("Cannot save training: training is null");
            throw new IllegalArgumentException("Training cannot be null");
        }
        log.info("Saving training with id {}", training.getId());
        storage.put(training.getId(), training);
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        if (id == null) {
            log.warn("Cannot find training: id is null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        Optional<Training> training = Optional.ofNullable(storage.get(id));
        log.debug("Training lookup for id {} returned {}", id, training.isPresent() ? "a result" : "no result");
        return training;
    }
}
