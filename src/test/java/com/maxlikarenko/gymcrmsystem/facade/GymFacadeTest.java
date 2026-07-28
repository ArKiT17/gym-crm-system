package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.service.TraineeService;
import com.maxlikarenko.gymcrmsystem.service.TrainerService;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GymFacadeTest {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;
    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingService = mock(TrainingService.class);
        gymFacade = new GymFacade(traineeService, trainerService, trainingService);
    }

    @Test
    void createTraineeDelegatesToTraineeService() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeService.create(trainee)).thenReturn(trainee);

        assertSame(trainee, gymFacade.createTrainee(trainee));

        verify(traineeService).create(trainee);
    }

    @Test
    void updateTraineeDelegatesToTraineeService() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeService.update(trainee)).thenReturn(trainee);

        assertSame(trainee, gymFacade.updateTrainee(trainee));

        verify(traineeService).update(trainee);
    }

    @Test
    void deleteTraineeDelegatesToTraineeService() {
        gymFacade.deleteTrainee(1L);

        verify(traineeService).delete(1L);
    }

    @Test
    void findTraineeByIdReturnsServiceResult() {
        Trainee trainee = Trainee.builder().id(1L).build();
        Optional<Trainee> expected = Optional.of(trainee);
        when(traineeService.findById(1L)).thenReturn(expected);

        assertEquals(expected, gymFacade.findTraineeById(1L));

        verify(traineeService).findById(1L);
    }

    @Test
    void createTrainerDelegatesToTrainerService() {
        Trainer trainer = Trainer.builder().id(10L).build();
        when(trainerService.create(trainer)).thenReturn(trainer);

        assertSame(trainer, gymFacade.createTrainer(trainer));

        verify(trainerService).create(trainer);
    }

    @Test
    void updateTrainerDelegatesToTrainerService() {
        Trainer trainer = Trainer.builder().id(10L).build();
        when(trainerService.update(trainer)).thenReturn(trainer);

        assertSame(trainer, gymFacade.updateTrainer(trainer));

        verify(trainerService).update(trainer);
    }

    @Test
    void findTrainerByIdReturnsServiceResult() {
        Trainer trainer = Trainer.builder().id(10L).build();
        Optional<Trainer> expected = Optional.of(trainer);
        when(trainerService.findById(10L)).thenReturn(expected);

        assertEquals(expected, gymFacade.findTrainerById(10L));

        verify(trainerService).findById(10L);
    }

    @Test
    void createTrainingDelegatesToTrainingService() {
        Training training = Training.builder().id(100L).build();
        when(trainingService.create(training)).thenReturn(training);

        assertSame(training, gymFacade.createTraining(training));

        verify(trainingService).create(training);
    }

    @Test
    void findTrainingByIdReturnsServiceResult() {
        Training training = Training.builder().id(100L).build();
        Optional<Training> expected = Optional.of(training);
        when(trainingService.findById(100L)).thenReturn(expected);

        assertEquals(expected, gymFacade.findTrainingById(100L));

        verify(trainingService).findById(100L);
    }
}
