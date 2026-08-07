package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {
    private UserAccountService userAccountService;
    private TrainerService trainerService;
    private TraineeRepository traineeRepository;
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        userAccountService = mock(UserAccountService.class);
        trainerService = mock(TrainerService.class);
        traineeRepository = mock(TraineeRepository.class);
        traineeService = new TraineeService();
        traineeService.setUserAccountService(userAccountService);
        traineeService.setTrainerService(trainerService);
        traineeService.setTraineeRepository(traineeRepository);
    }

    @Test
    void createGeneratesCredentialsAndSavesTrainee() {
        User user = new User("John", "Smith");
        Trainee trainee = new Trainee(user, null, null);
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        assertSame(trainee, traineeService.create(trainee));
        verify(userAccountService).generateCredentials(user);
        verify(traineeRepository).save(trainee);
    }

    @Test
    void createRejectsNullTrainee() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.create(null));
        verifyNoInteractions(userAccountService, traineeRepository);
    }

    @Test
    void updateChangesTraineeFieldsByUsername() {
        Trainee trainee = new Trainee(new User("John", "Smith"), null, null);
        LocalDate birthDate = LocalDate.of(2000, 1, 1);
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.update(
                "John.Smith", "Johnny", "Doe", birthDate, "Kyiv", false);

        assertSame(trainee, result);
        assertAll(
                () -> assertEquals("Johnny", trainee.getUser().getFirstName()),
                () -> assertEquals("Doe", trainee.getUser().getLastName()),
                () -> assertEquals(birthDate, trainee.getDateOfBirth()),
                () -> assertEquals("Kyiv", trainee.getAddress()),
                () -> assertFalse(trainee.getUser().isActive())
        );
    }

    @Test
    void getByIdAndUsernameReturnTrainee() {
        Trainee trainee = new Trainee(new User("John", "Smith"), null, null);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertAll(
                () -> assertSame(trainee, traineeService.get(1L)),
                () -> assertSame(trainee, traineeService.get("John.Smith"))
        );
    }

    @Test
    void getThrowsWhenTraineeDoesNotExist() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());
        when(traineeRepository.findByUserUsername("missing")).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(ResourceNotFoundException.class, () -> traineeService.get(1L)),
                () -> assertThrows(ResourceNotFoundException.class, () -> traineeService.get("missing"))
        );
    }

    @Test
    void deleteByIdClearsTrainersAndDeletesEntity() {
        Trainee trainee = new Trainee(new User("John", "Smith"), null, null);
        Trainer trainer = new Trainer(new User("Jane", "Doe"), null);
        trainee.getTrainers().add(trainer);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        traineeService.delete(1L);

        assertTrue(trainee.getTrainers().isEmpty());
        verify(traineeRepository).deleteById(1L);
    }

    @Test
    void deleteByUsernameClearsTrainersAndDeletesEntity() {
        Trainee trainee = new Trainee(new User("John", "Smith"), null, null);
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.delete("John.Smith");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void updateTrainersReplacesExistingTrainers() {
        Trainee trainee = new Trainee(new User("John", "Smith"), null, null);
        Trainer trainer = new Trainer(new User("Jane", "Doe"), null);
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerService.getActiveByUsernames(Set.of("Jane.Doe"))).thenReturn(Set.of(trainer));

        assertEquals(Set.of(trainer), traineeService.updateTrainers("John.Smith", Set.of("Jane.Doe")));
    }
}
