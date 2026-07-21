package com.maxlikarenko.gymcrmsystem.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class StorageInitializer {

private final String filePath;
private final Map<Long, Trainee> trainees;
private final Map<Long, Trainer> trainers;
private final Map<Long, Training> trainings;
private final ObjectMapper objectMapper;

public StorageInitializer(@Value("${storage.file}") String filePath,
                           @Qualifier("traineeStorage") Map<Long, Trainee> trainees,
                           @Qualifier("trainerStorage") Map<Long, Trainer> trainers,
                           @Qualifier("trainingStorage") Map<Long, Training> trainings,
                           ObjectMapper objectMapper) {
    this.filePath = filePath;
    this.trainees = trainees;
    this.trainers = trainers;
    this.trainings = trainings;
    this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("Loading in-memory storage from classpath resource '{}'", filePath);
        try (var inputStream = new ClassPathResource(filePath).getInputStream()) {
            StorageData loadedStorage = objectMapper.readValue(inputStream, StorageData.class);
            trainees.putAll(loadedStorage.getTrainees());
            trainers.putAll(loadedStorage.getTrainers());
            trainings.putAll(loadedStorage.getTrainings());
            log.info("In-memory storage loaded: {} trainees, {} trainers, {} trainings",
                    loadedStorage.getTrainees().size(),
                    loadedStorage.getTrainers().size(),
                    loadedStorage.getTrainings().size());
        } catch (IOException e) {
            log.error("Failed to load in-memory storage from classpath resource '{}'. ", filePath, e);
            throw new IllegalStateException("Failed to load storage from '" + filePath + "'", e);
        }
    }
}
