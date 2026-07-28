package com.maxlikarenko.gymcrmsystem.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class StorageInitializer {

    private String filePath;
    private Map<Long, Trainee> trainees;
    private Map<Long, Trainer> trainers;
    private Map<Long, Training> trainings;
    private ObjectMapper objectMapper;

    @Value("${storage.file}")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Autowired
    public void setTrainees(@Qualifier("traineeStorage") Map<Long, Trainee> trainees) {
        this.trainees = trainees;
    }

    @Autowired
    public void setTrainers(@Qualifier("trainerStorage") Map<Long, Trainer> trainers) {
        this.trainers = trainers;
    }

    @Autowired
    public void setTrainings(@Qualifier("trainingStorage") Map<Long, Training> trainings) {
        this.trainings = trainings;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
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
