package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainer;

import java.util.Optional;

public interface TrainerRepository {
    Trainer save(Trainer trainer);
    Optional<Trainer> findById(Long id);
}
