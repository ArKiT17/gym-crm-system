package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.storage.InMemoryStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Repository
public class InMemoryTrainerRepository implements TrainerRepository {
    private final InMemoryStorage storage;

    @Override
    public Trainer save(Trainer trainer) {
        if (trainer == null) {
            log.warn("Cannot save trainer: trainer is null");
            throw new IllegalArgumentException("Trainer cannot be null");
        }
        log.info("Saving trainer with id {}", trainer.getId());
        storage.getTrainers().put(trainer.getId(), trainer);
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        if (id == null) {
            log.warn("Cannot find trainer: id is null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        Optional<Trainer> trainer = Optional.ofNullable(storage.getTrainers().get(id));
        log.debug("Trainer lookup for id {} returned {}", id, trainer.isPresent() ? "a result" : "no result");
        return trainer;
    }
}
