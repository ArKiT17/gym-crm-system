package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTraineeRepositoryTest {

    private Map<Long, Trainee> storage;
    private InMemoryTraineeRepository repository;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        repository = new InMemoryTraineeRepository();
        repository.setStorage(storage);
    }

    @Test
    void saveRejectsNullTrainee() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void saveStoresAndReturnsTrainee() {
        Trainee trainee = Trainee.builder().id(1L).firstName("John").build();

        Trainee savedTrainee = repository.save(trainee);

        assertSame(trainee, savedTrainee);
        assertEquals(trainee, repository.findById(1L).orElseThrow());
    }

    @Test
    void saveReplacesTraineeWithTheSameId() {
        Trainee firstTrainee = Trainee.builder().id(1L).firstName("John").build();
        Trainee replacementTrainee = Trainee.builder().id(1L).firstName("Jane").build();

        repository.save(firstTrainee);
        repository.save(replacementTrainee);

        assertEquals(1, storage.size());
        assertSame(replacementTrainee, repository.findById(1L).orElseThrow());
    }

    @Test
    void findByIdRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }

    @Test
    void findByIdReturnsExistingTraineeById() {
        Trainee trainee = Trainee.builder().id(1L).firstName("John").build();
        repository.save(trainee);

        Trainee foundTrainee = repository.findById(1L).orElseThrow();

        assertSame(trainee, foundTrainee);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(repository.findById(999L).isEmpty());
    }

    @Test
    void deleteByIdRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> repository.deleteById(null));
    }

    @Test
    void deleteByIdRemovesExistingTrainee() {
        repository.save(Trainee.builder().id(1L).build());

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteByIdDoesNothingForUnknownId() {
        repository.deleteById(999L);

        assertTrue(storage.isEmpty());
    }
}
