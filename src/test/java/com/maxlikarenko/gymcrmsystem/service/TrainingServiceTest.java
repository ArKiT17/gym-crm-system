package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingServiceTest {

    private TrainingRepository trainingRepository;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingRepository = mock(TrainingRepository.class);
        trainingService = new TrainingService();
        trainingService.setTrainingRepository(trainingRepository);
    }

    @Test
    void createRejectsNullTraining() {
        assertThrows(IllegalArgumentException.class, () -> trainingService.create(null));
    }

    @Test
    void createSavesAndReturnsTraining() {
        Training training = Training.builder()
                .id(100L)
                .name("Morning Strength")
                .build();
        when(trainingRepository.save(training)).thenReturn(training);

        Training result = trainingService.create(training);

        assertSame(training, result);
        verify(trainingRepository).save(training);
    }

    @Test
    void findByIdReturnsFoundTraining() {
        Training training = Training.builder().id(100L).build();
        when(trainingRepository.findById(100L)).thenReturn(Optional.of(training));

        assertEquals(Optional.of(training), trainingService.findById(100L));
    }

    @Test
    void findByIdReturnsEmptyWhenTrainingDoesNotExist() {
        when(trainingRepository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(trainingService.findById(999L).isEmpty());
    }
}
