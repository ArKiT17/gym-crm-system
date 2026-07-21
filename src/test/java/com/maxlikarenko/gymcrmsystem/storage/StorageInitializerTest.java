package com.maxlikarenko.gymcrmsystem.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageInitializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
    }

    @Test
    void initLoadsStorageFromClasspathResource() {
        createInitializer("test-storage.json", storage).init();

        assertEquals(5, storage.getTrainees().size());
        assertEquals(5, storage.getTrainers().size());
        assertEquals(5, storage.getTrainings().size());
        assertEquals("John", storage.getTrainees().get(1L).getFirstName());
        assertEquals("Strength", storage.getTrainers().get(10L).getSpecialization().getName());
        assertEquals(Duration.ofHours(1), storage.getTrainings().get(100L).getDuration());
    }

    @Test
    void initLoadsEmptyStorageWhenResourceIsEmpty() {
        createInitializer("empty-storage.json", storage).init();

        assertTrue(storage.getTrainees().isEmpty());
        assertTrue(storage.getTrainers().isEmpty());
        assertTrue(storage.getTrainings().isEmpty());
    }

    @Test
    void initThrowsWhenResourceIsMissing() {
        assertThrows(IllegalStateException.class,
                () -> createInitializer("missing-storage.json", storage).init());

        assertTrue(storage.getTrainees().isEmpty());
        assertTrue(storage.getTrainers().isEmpty());
        assertTrue(storage.getTrainings().isEmpty());
    }

    @Test
    void initThrowsWhenResourceContainsInvalidJson() {
        assertThrows(IllegalStateException.class,
                () -> createInitializer("invalid-storage.json", storage).init());

        assertTrue(storage.getTrainees().isEmpty());
        assertTrue(storage.getTrainers().isEmpty());
        assertTrue(storage.getTrainings().isEmpty());
    }

    @Test
    void initThrowsWhenResourceHasInvalidStructure() {
        assertThrows(IllegalStateException.class,
                () -> createInitializer("invalid-structure-storage.json", storage).init());

        assertTrue(storage.getTrainees().isEmpty());
        assertTrue(storage.getTrainers().isEmpty());
        assertTrue(storage.getTrainings().isEmpty());
    }

    private StorageInitializer createInitializer(String filePath, InMemoryStorage storage) {
        return new StorageInitializer(filePath, storage, objectMapper);
    }
}
