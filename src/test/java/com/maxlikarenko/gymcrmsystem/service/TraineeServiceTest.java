package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
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

    private User user(String first, String last) {
        return new User(first, last, first + "." + last, "pass", true);
    }

    private Trainee trainee(User u) {
        return new Trainee(u, null, null, new HashSet<>(), new HashSet<>());
    }

    // create

    @Test
    void createThrowsForNullTrainee() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.create(null));
        verifyNoInteractions(traineeRepository, userAccountService);
    }

    @Test
    void createThrowsWhenUserIsNull() {
        Trainee t = new Trainee(null, null, null, new HashSet<>(), new HashSet<>());
        doThrow(new IllegalArgumentException("User cannot be null"))
                .when(userAccountService).prepareForRegistration(null);

        assertThrows(IllegalArgumentException.class, () -> traineeService.create(t));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void createCallsPrepareForRegistrationOnUser() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.save(t)).thenReturn(t);

        traineeService.create(t);

        verify(userAccountService).prepareForRegistration(u);
    }

    @Test
    void createSavesAndReturnsTrainee() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.save(t)).thenReturn(t);

        assertSame(t, traineeService.create(t));
        verify(traineeRepository).save(t);
    }

    // update

    @Test
    void updateThrowsForNullTrainee() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.update(null));
        verifyNoInteractions(traineeRepository, userAccountService);
    }

    @Test
    void updateCallsValidateUserOnUser() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.save(t)).thenReturn(t);

        traineeService.update(t);

        verify(userAccountService).validateUser(u);
    }

    @Test
    void updateDoesNotCallPrepareForRegistration() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.save(t)).thenReturn(t);

        traineeService.update(t);

        verify(userAccountService, never()).prepareForRegistration(any());
    }

    @Test
    void updateSavesAndReturnsTrainee() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.save(t)).thenReturn(t);

        assertSame(t, traineeService.update(t));
        verify(traineeRepository).save(t);
    }

    // delete by id

    @Test
    void deleteByIdThrowsWhenTraineeNotFound() {
        when(traineeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.delete(999L));
    }

    @Test
    void deleteByIdClearsTrainersAndDeletesById() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(t));

        traineeService.delete(1L);

        assertTrue(t.getTrainers().isEmpty());
        verify(traineeRepository).deleteById(1L);
    }

    // delete by username

    @Test
    void deleteByUsernameThrowsWhenTraineeNotFound() {
        when(traineeRepository.findByUserUsername("missing")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.delete("missing"));
    }

    @Test
    void deleteByUsernameClearsTrainersAndDeletesEntity() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(t));

        traineeService.delete("John.Smith");

        assertTrue(t.getTrainers().isEmpty());
        verify(traineeRepository).delete(t);
    }

    // find by id

    @Test
    void findByIdReturnsPresentWhenFound() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), traineeService.find(1L));
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(traineeRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(traineeService.find(999L).isEmpty());
    }

    // find by username

    @Test
    void findByUsernameReturnsPresentWhenFound() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), traineeService.find("John.Smith"));
    }

    @Test
    void findByUsernameReturnsEmptyWhenNotFound() {
        when(traineeRepository.findByUserUsername("missing")).thenReturn(Optional.empty());
        assertTrue(traineeService.find("missing").isEmpty());
    }

    // updateTrainers

    @Test
    void updateTrainersThrowsWhenTraineeNotFound() {
        when(traineeRepository.findByUserUsername("missing")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainers("missing", Set.of("t1")));
    }

    @Test
    void updateTrainersPropagatesEntityNotFoundFromTrainerService() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(t));
        Set<String> names = Set.of("missing.trainer");
        when(trainerService.findAll(names))
                .thenThrow(new EntityNotFoundException("Trainer not found: missing.trainer"));

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainers("John.Smith", names));
    }

    @Test
    void updateTrainersReplacesTrainerSet() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        User trainerUser = new User("Jane", "Doe", "Jane.Doe", "pass", true);
        Trainer trainer = new Trainer(trainerUser, null, new HashSet<>(), new HashSet<>());

        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(t));
        Set<String> names = Set.of("Jane.Doe");
        when(trainerService.findAll(names)).thenReturn(Set.of(trainer));

        Set<Trainer> result = traineeService.updateTrainers("John.Smith", names);

        assertEquals(1, result.size());
        assertTrue(result.contains(trainer));
    }
}
