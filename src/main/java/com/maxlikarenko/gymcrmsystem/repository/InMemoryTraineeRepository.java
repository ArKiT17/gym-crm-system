package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.storage.InMemoryStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Repository
public class InMemoryTraineeRepository implements TraineeRepository {
    private final InMemoryStorage storage;

    @Override
    public Trainee save(Trainee trainee) {
        if (trainee == null) {
            log.warn("Cannot save trainee: trainee is null");
            throw new IllegalArgumentException("Trainee cannot be null");
        }
        log.info("Saving trainee with id {}", trainee.getId());
        storage.getTrainees().put(trainee.getId(), trainee);
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        if (id == null) {
            log.warn("Cannot find trainee: id is null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        Optional<Trainee> trainee = Optional.ofNullable(storage.getTrainees().get(id));
        log.debug("Trainee lookup for id {} returned {}", id, trainee.isPresent() ? "a result" : "no result");
        return trainee;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            log.warn("Cannot delete trainee: id is null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        Trainee removedTrainee = storage.getTrainees().remove(id);
        if (removedTrainee == null) {
            log.warn("Trainee with id {} was not found for deletion", id);
            return;
        }
        log.info("Deleted trainee with id {}", id);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) {
            log.warn("Cannot check existence: username is null");
            throw new IllegalArgumentException("Username cannot be null");
        }
        boolean exists = storage.getTrainees().values().stream()
                .anyMatch(trainee -> username.equals(trainee.getUsername()));
        log.debug("Existence check for username {} returned {}", username, exists);
        return exists;
    }
}
