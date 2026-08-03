package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingTypeServiceTest {

    private TrainingTypeRepository trainingTypeRepository;
    private TrainingTypeService trainingTypeService;

    @BeforeEach
    void setUp() {
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        trainingTypeService = new TrainingTypeService();
        trainingTypeService.setTrainingTypeRepository(trainingTypeRepository);
    }

    @Test
    void findReturnsPresentWhenTypeExists() {
        TrainingType type = new TrainingType("yoga");
        when(trainingTypeRepository.findByName("yoga")).thenReturn(Optional.of(type));

        Optional<TrainingType> result = trainingTypeService.find("yoga");

        assertTrue(result.isPresent());
        assertEquals("yoga", result.get().getName());
    }

    @Test
    void findReturnsEmptyWhenTypeDoesNotExist() {
        when(trainingTypeRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertTrue(trainingTypeService.find("unknown").isEmpty());
    }

    @Test
    void findDelegatesToRepositoryWithExactName() {
        trainingTypeService.find("fitness");
        verify(trainingTypeRepository).findByName("fitness");
    }
}
