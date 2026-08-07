package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingTypeRepository;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
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
    void getByIdReturnsTrainingType() {
        TrainingType type = new TrainingType("yoga");
        ReflectionTestUtils.setField(type, "id", 1L);
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(type));

        assertSame(type, trainingTypeService.get(1L));
    }

    @Test
    void getByNameReturnsTrainingType() {
        TrainingType type = new TrainingType("yoga");
        when(trainingTypeRepository.findByName("yoga")).thenReturn(Optional.of(type));

        assertSame(type, trainingTypeService.get("yoga"));
    }

    @Test
    void getThrowsWhenTrainingTypeDoesNotExist() {
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.empty());
        when(trainingTypeRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(ResourceNotFoundException.class, () -> trainingTypeService.get(1L)),
                () -> assertThrows(ResourceNotFoundException.class, () -> trainingTypeService.get("unknown"))
        );
    }

    @Test
    void getAllDelegatesToRepository() {
        List<TrainingType> types = List.of(new TrainingType("yoga"));
        when(trainingTypeRepository.findAll()).thenReturn(types);

        assertSame(types, trainingTypeService.getAll());
    }
}
