package com.maxlikarenko.gymcrmsystem.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageInitializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private Map<Long, Trainee> trainees;
    private Map<Long, Trainer> trainers;
    private Map<Long, Training> trainings;

    @BeforeEach
    void setUp() {
        trainees = new HashMap<>();
        trainers = new HashMap<>();
        trainings = new HashMap<>();
    }

    @Test
    void initLoadsStorageFromClasspathResource() {
        createInitializer("test-storage.json").init();

        assertEquals(5, trainees.size());
        assertEquals(5, trainers.size());
        assertEquals(5, trainings.size());
        assertEquals("John", trainees.get(1L).getFirstName());
        assertEquals("Strength", trainers.get(10L).getSpecialization().getName());
        assertEquals(Duration.ofHours(1), trainings.get(100L).getDuration());
    }

    @Test
    void initLoadsEmptyStorageWhenResourceIsEmpty() {
        createInitializer("empty-storage.json").init();

        assertTrue(trainees.isEmpty());
        assertTrue(trainers.isEmpty());
        assertTrue(trainings.isEmpty());
    }

    @Test
    void initThrowsWhenResourceIsMissing() {
        assertThrows(IllegalStateException.class,
                () -> createInitializer("missing-storage.json").init());

        assertTrue(trainees.isEmpty());
        assertTrue(trainers.isEmpty());
        assertTrue(trainings.isEmpty());
    }

    @Test
    void initThrowsWhenResourceContainsInvalidJson() {
        assertThrows(IllegalStateException.class,
                () -> createInitializer("invalid-storage.json").init());

        assertTrue(trainees.isEmpty());
        assertTrue(trainers.isEmpty());
        assertTrue(trainings.isEmpty());
    }

    @Test
    void initThrowsWhenResourceHasInvalidStructure() {
        assertThrows(IllegalStateException.class,
                () -> createInitializer("invalid-structure-storage.json").init());

        assertTrue(trainees.isEmpty());
        assertTrue(trainers.isEmpty());
        assertTrue(trainings.isEmpty());
    }

    private StorageInitializer createInitializer(String filePath) {
        StorageInitializer initializer = new StorageInitializer();
        initializer.setFilePath(filePath);
        initializer.setTrainees(trainees);
        initializer.setTrainers(trainers);
        initializer.setTrainings(trainings);
        initializer.setObjectMapper(objectMapper);
        return initializer;
    }
}
