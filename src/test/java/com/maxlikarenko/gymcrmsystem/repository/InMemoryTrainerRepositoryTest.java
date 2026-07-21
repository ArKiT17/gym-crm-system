package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTrainerRepositoryTest {

    private Map<Long, Trainer> storage;
    private InMemoryTrainerRepository repository;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        repository = new InMemoryTrainerRepository(storage);
    }

    @Test
    void saveRejectsNullTrainer() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void saveStoresAndReturnsTrainer() {
        Trainer trainer = Trainer.builder().id(10L).firstName("Jane").build();

        Trainer savedTrainer = repository.save(trainer);

        assertSame(trainer, savedTrainer);
        assertSame(trainer, storage.get(10L));
        assertEquals(trainer, repository.findById(10L).orElseThrow());
    }

    @Test
    void saveReplacesTrainerWithTheSameId() {
        Trainer firstTrainer = Trainer.builder().id(10L).firstName("Jane").build();
        Trainer replacementTrainer = Trainer.builder().id(10L).firstName("Robert").build();

        repository.save(firstTrainer);
        repository.save(replacementTrainer);

        assertEquals(1, storage.size());
        assertSame(replacementTrainer, repository.findById(10L).orElseThrow());
    }

    @Test
    void findByIdRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }

    @Test
    void findByIdReturnsExistingTrainerById() {
        Trainer trainer = Trainer.builder().id(10L).firstName("Jane").build();
        repository.save(trainer);

        Trainer foundTrainer = repository.findById(10L).orElseThrow();

        assertSame(trainer, foundTrainer);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(repository.findById(999L).isEmpty());
    }
}
