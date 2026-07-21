package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Training;

import java.util.Optional;

public interface TrainingRepository {
    Training save(Training training);
    Optional<Training> findById(Long id);
}
