package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import com.maxlikarenko.gymcrmsystem.exception.ConflictException;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingServiceTest {
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingRepository trainingRepository;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingRepository = mock(TrainingRepository.class);
        trainingService = new TrainingService(traineeService, trainerService, trainingRepository);
    }

    @Test
    void addTrainingUsesTrainerSpecializationAndSavesTraining() {
        Trainee trainee = new Trainee(new User("John", "Smith"), null, null);
        TrainingType specialization = new TrainingType("yoga");
        Trainer trainer = new Trainer(new User("Jane", "Doe"), specialization);
        LocalDate date = LocalDate.of(2025, 1, 1);
        Training saved = new Training(trainee, trainer, "Yoga", specialization, date, 60);
        when(traineeService.get("John.Smith")).thenReturn(trainee);
        when(trainerService.get("Jane.Doe")).thenReturn(trainer);
        when(trainingRepository.save(any(Training.class))).thenReturn(saved);

        Training result = trainingService.addTraining(
                "John.Smith", "Jane.Doe", "Yoga", date, 60);

        assertSame(saved, result);
        verify(trainingRepository).save(any(Training.class));
        assertSame(specialization, result.getType());
    }

    @Test
    void addTrainingRejectsTrainerWithoutSpecialization() {
        when(traineeService.get("John.Smith"))
                .thenReturn(new Trainee(new User("John", "Smith"), null, null));
        when(trainerService.get("Jane.Doe"))
                .thenReturn(new Trainer(new User("Jane", "Doe"), null));

        assertThrows(ConflictException.class,
                () -> trainingService.addTraining(
                        "John.Smith", "Jane.Doe", "Training", LocalDate.now(), 60));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void addTrainingRejectsInactiveTrainee() {
        User traineeUser = new User("John", "Smith");
        traineeUser.setActive(false);
        when(traineeService.get("John.Smith"))
                .thenReturn(new Trainee(traineeUser, null, null));
        when(trainerService.get("Jane.Doe"))
                .thenReturn(new Trainer(new User("Jane", "Doe"), new TrainingType("yoga")));

        assertThrows(ConflictException.class,
                () -> trainingService.addTraining(
                        "John.Smith", "Jane.Doe", "Training", LocalDate.now(), 60));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void addTrainingRejectsInactiveTrainer() {
        User trainerUser = new User("Jane", "Doe");
        trainerUser.setActive(false);
        when(traineeService.get("John.Smith"))
                .thenReturn(new Trainee(new User("John", "Smith"), null, null));
        when(trainerService.get("Jane.Doe"))
                .thenReturn(new Trainer(trainerUser, new TrainingType("yoga")));

        assertThrows(ConflictException.class,
                () -> trainingService.addTraining(
                        "John.Smith", "Jane.Doe", "Training", LocalDate.now(), 60));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void addTrainingPropagatesMissingTrainee() {
        when(traineeService.get("missing"))
                .thenThrow(new ResourceNotFoundException("Trainee not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> trainingService.addTraining(
                        "missing", "Jane.Doe", "Training", LocalDate.now(), 60));

        verifyNoInteractions(trainerService, trainingRepository);
    }

    @Test
    void getReturnsTrainingOrThrowsWhenMissing() {
        Training training = training();
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
        when(trainingRepository.findById(2L)).thenReturn(Optional.empty());

        assertAll(
                () -> assertSame(training, trainingService.get(1L)),
                () -> assertThrows(ResourceNotFoundException.class, () -> trainingService.get(2L))
        );
    }

    @Test
    void getTraineeTrainingsDelegatesAllFilters() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        when(trainingRepository.findTraineeTrainings(
                "John.Smith", from, to, "Jane.Doe", "yoga")).thenReturn(Set.of());

        trainingService.getTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga");

        verify(trainingRepository).findTraineeTrainings(
                "John.Smith", from, to, "Jane.Doe", "yoga");
    }

    @Test
    void getTrainerTrainingsDelegatesAllFilters() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        when(trainingRepository.findTrainerTrainings(
                "Jane.Doe", from, to, "John.Smith")).thenReturn(Set.of());

        trainingService.getTrainerTrainings("Jane.Doe", from, to, "John.Smith");

        verify(trainingRepository).findTrainerTrainings(
                "Jane.Doe", from, to, "John.Smith");
    }

    private Training training() {
        return new Training(
                new Trainee(new User("John", "Smith"), null, null),
                new Trainer(new User("Jane", "Doe"), new TrainingType("yoga")),
                "Yoga",
                new TrainingType("yoga"),
                LocalDate.now(),
                60
        );
    }
}
