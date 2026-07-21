package com.maxlikarenko.gymcrmsystem.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class StorageInitializer {

    private final String filePath;
    private final InMemoryStorage storage;
    private final ObjectMapper objectMapper;

    public StorageInitializer(@Value("${storage.file}") String filePath,
                              InMemoryStorage storage, ObjectMapper objectMapper) {
        this.filePath = filePath;
        this.storage = storage;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("Loading in-memory storage from classpath resource '{}'", filePath);
        try (var inputStream = new ClassPathResource(filePath).getInputStream()) {
            InMemoryStorage loadedStorage = objectMapper.readValue(inputStream, InMemoryStorage.class);
            storage.getTrainees().putAll(loadedStorage.getTrainees());
            storage.getTrainers().putAll(loadedStorage.getTrainers());
            storage.getTrainings().putAll(loadedStorage.getTrainings());
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
