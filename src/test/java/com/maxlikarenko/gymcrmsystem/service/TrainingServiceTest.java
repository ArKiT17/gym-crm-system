package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.*;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingTypeService trainingTypeService;
    private TrainingRepository trainingRepository;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingTypeService = mock(TrainingTypeService.class);
        trainingRepository = mock(TrainingRepository.class);
        trainingService = new TrainingService();
        trainingService.setTraineeService(traineeService);
        trainingService.setTrainerService(trainerService);
        trainingService.setTrainingTypeService(trainingTypeService);
        trainingService.setTrainingRepository(trainingRepository);
    }

    private Trainee makeTrainee() {
        User u = new User("John", "Smith", "John.Smith", "pass", true);
        return new Trainee(u, null, null, new HashSet<>(), new HashSet<>());
    }

    private Trainer makeTrainer() {
        User u = new User("Jane", "Doe", "Jane.Doe", "pass", true);
        return new Trainer(u, null, new HashSet<>(), new HashSet<>());
    }

    private TrainingType yoga() {
        return new TrainingType("yoga");
    }

    // addTraining: required-field validation

    @Test
    void addTrainingThrowsWhenTraineeUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("", "Jane.Doe", "T", "yoga", LocalDate.now(), 60));
        verifyNoInteractions(traineeService, trainerService, trainingTypeService, trainingRepository);
    }

    @Test
    void addTrainingThrowsWhenTraineeUsernameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining(null, "Jane.Doe", "T", "yoga", LocalDate.now(), 60));
    }

    @Test
    void addTrainingThrowsWhenTrainerUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "", "T", "yoga", LocalDate.now(), 60));
    }

    @Test
    void addTrainingThrowsWhenTrainerUsernameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", null, "T", "yoga", LocalDate.now(), 60));
    }

    @Test
    void addTrainingThrowsWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "  ", "yoga", LocalDate.now(), 60));
    }

    @Test
    void addTrainingThrowsWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", null, "yoga", LocalDate.now(), 60));
    }

    @Test
    void addTrainingThrowsWhenTypeIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "T", "", LocalDate.now(), 60));
    }

    @Test
    void addTrainingThrowsWhenTypeIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "T", null, LocalDate.now(), 60));
    }

    @Test
    void addTrainingThrowsWhenDateIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "T", "yoga", null, 60));
    }

    @Test
    void addTrainingThrowsWhenDurationIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "T", "yoga", LocalDate.now(), 0));
    }

    @Test
    void addTrainingThrowsWhenDurationIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "T", "yoga", LocalDate.now(), -5));
    }

    // addTraining: entity-not-found cases

    @Test
    void addTrainingThrowsWhenTraineeNotFound() {
        when(traineeService.find("John.Smith")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "T", "yoga", LocalDate.now(), 60));
        verifyNoInteractions(trainerService, trainingTypeService, trainingRepository);
    }

    @Test
    void addTrainingThrowsWhenTrainerNotFound() {
        when(traineeService.find("John.Smith")).thenReturn(Optional.of(makeTrainee()));
        when(trainerService.find("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining("John.Smith", "missing", "T", "yoga", LocalDate.now(), 60));
        verifyNoInteractions(trainingTypeService, trainingRepository);
    }

    @Test
    void addTrainingThrowsWhenTrainingTypeNotFound() {
        when(traineeService.find("John.Smith")).thenReturn(Optional.of(makeTrainee()));
        when(trainerService.find("Jane.Doe")).thenReturn(Optional.of(makeTrainer()));
        when(trainingTypeService.find("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining("John.Smith", "Jane.Doe", "T", "unknown", LocalDate.now(), 60));
        verifyNoInteractions(trainingRepository);
    }

    // addTraining: success and boundary

    @Test
    void addTrainingWithDurationOfOneSavesSuccessfully() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        Trainee trainee = makeTrainee();
        Trainer trainer = makeTrainer();
        TrainingType type = yoga();

        when(traineeService.find("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerService.find("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.find("yoga")).thenReturn(Optional.of(type));

        Training saved = new Training(trainee, trainer, "T", type, date, 1);
        when(trainingRepository.save(any(Training.class))).thenReturn(saved);

        Training result = trainingService.addTraining("John.Smith", "Jane.Doe", "T", "yoga", date, 1);
        assertNotNull(result);
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void addTrainingSavesAndReturnsTraining() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        Trainee trainee = makeTrainee();
        Trainer trainer = makeTrainer();
        TrainingType type = yoga();

        when(traineeService.find("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerService.find("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.find("yoga")).thenReturn(Optional.of(type));

        Training saved = new Training(trainee, trainer, "Yoga Session", type, date, 90);
        when(trainingRepository.save(any(Training.class))).thenReturn(saved);

        Training result = trainingService.addTraining("John.Smith", "Jane.Doe", "Yoga Session", "yoga", date, 90);

        assertSame(saved, result);
        verify(trainingRepository).save(any(Training.class));
    }

    // find

    @Test
    void findReturnsPresentWhenFound() {
        Trainee trainee = makeTrainee();
        Trainer trainer = makeTrainer();
        Training training = new Training(trainee, trainer, "T", yoga(), LocalDate.now(), 60);
        when(trainingRepository.findById(100L)).thenReturn(Optional.of(training));

        assertEquals(Optional.of(training), trainingService.find(100L));
    }

    @Test
    void findReturnsEmptyWhenNotFound() {
        when(trainingRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(trainingService.find(999L).isEmpty());
    }

    // getTraineeTrainings delegation

    @Test
    void getTraineeTrainingsDelegatesToRepository() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        when(trainingRepository.findTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga"))
                .thenReturn(Set.of());

        trainingService.getTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga");

        verify(trainingRepository).findTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga");
    }

    @Test
    void getTraineeTrainingsWithNullFiltersIsAllowed() {
        when(trainingRepository.findTraineeTrainings("John.Smith", null, null, null, null))
                .thenReturn(Set.of());

        assertDoesNotThrow(() ->
                trainingService.getTraineeTrainings("John.Smith", null, null, null, null));
    }

    // getTrainerTrainings delegation

    @Test
    void getTrainerTrainingsDelegatesToRepository() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        when(trainingRepository.findTrainerTrainings("Jane.Doe", from, to, "John.Smith"))
                .thenReturn(Set.of());

        trainingService.getTrainerTrainings("Jane.Doe", from, to, "John.Smith");

        verify(trainingRepository).findTrainerTrainings("Jane.Doe", from, to, "John.Smith");
    }

    @Test
    void getTrainerTrainingsWithNullTraineeFilterIsAllowed() {
        when(trainingRepository.findTrainerTrainings("Jane.Doe", null, null, null))
                .thenReturn(Set.of());

        assertDoesNotThrow(() ->
                trainingService.getTrainerTrainings("Jane.Doe", null, null, null));
    }
}
