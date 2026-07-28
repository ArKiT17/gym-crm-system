package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainee;

import java.util.Optional;

public interface TraineeRepository {
    Trainee save(Trainee trainee);
    void deleteById(Long id);
    Optional<Trainee> findById(Long id);
    boolean existsByUsername(String username);
}
