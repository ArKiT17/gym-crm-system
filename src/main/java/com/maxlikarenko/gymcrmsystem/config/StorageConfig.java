package com.maxlikarenko.gymcrmsystem.config;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class StorageConfig {

    @Bean("traineeStorage")
    public Map<Long, Trainee> traineeStorage() {
        return new HashMap<>();
    }

    @Bean("trainerStorage")
    public Map<Long, Trainer> trainerStorage() {
        return new HashMap<>();
    }

    @Bean("trainingStorage")
    public Map<Long, Training> trainingStorage() {
        return new HashMap<>();
    }
}
