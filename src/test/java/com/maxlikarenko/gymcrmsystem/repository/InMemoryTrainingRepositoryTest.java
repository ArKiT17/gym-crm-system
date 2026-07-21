package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTrainingRepositoryTest {

    private Map<Long, Training> storage;
    private InMemoryTrainingRepository repository;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        repository = new InMemoryTrainingRepository();
        repository.setStorage(storage);
    }

    @Test
    void saveRejectsNullTraining() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void saveStoresAndReturnsTraining() {
        Training training = Training.builder().id(100L).name("Morning Strength").build();

        Training savedTraining = repository.save(training);

        assertSame(training, savedTraining);
        assertSame(training, storage.get(100L));
        assertEquals(training, repository.findById(100L).orElseThrow());
    }

    @Test
    void saveReplacesTrainingWithTheSameId() {
        Training firstTraining = Training.builder().id(100L).name("Morning Strength").build();
        Training replacementTraining = Training.builder().id(100L).name("Evening Strength").build();

        repository.save(firstTraining);
        repository.save(replacementTraining);

        assertEquals(1, storage.size());
        assertSame(replacementTraining, repository.findById(100L).orElseThrow());
    }

    @Test
    void findByIdRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }

    @Test
    void findByIdReturnsExistingTrainingById() {
        Training training = Training.builder().id(100L).name("Morning Strength").build();
        repository.save(training);

        Training foundTraining = repository.findById(100L).orElseThrow();

        assertSame(training, foundTraining);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(repository.findById(999L).isEmpty());
    }
}
