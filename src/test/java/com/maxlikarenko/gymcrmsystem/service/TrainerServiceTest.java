package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private UserAccountService userAccountService;
    private TrainerRepository trainerRepository;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        userAccountService = mock(UserAccountService.class);
        trainerRepository = mock(TrainerRepository.class);
        trainerService = new TrainerService();
        trainerService.setUserAccountService(userAccountService);
        trainerService.setTrainerRepository(trainerRepository);
    }

    private User user(String first, String last) {
        return new User(first, last, first + "." + last, "pass", true);
    }

    private Trainer trainer(User u) {
        return new Trainer(u, null, new HashSet<>(), new HashSet<>());
    }

    private Trainer trainerWithId(User u, long id) {
        Trainer t = trainer(u);
        ReflectionTestUtils.setField(t, "id", id);
        return t;
    }

    // create

    @Test
    void createThrowsForNullTrainer() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.create(null));
        verifyNoInteractions(trainerRepository, userAccountService);
    }

    @Test
    void createThrowsWhenUserIsNull() {
        Trainer t = new Trainer(null, null, new HashSet<>(), new HashSet<>());
        doThrow(new IllegalArgumentException("User cannot be null"))
                .when(userAccountService).prepareForRegistration(null);

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(t));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void createCallsPrepareForRegistrationOnUser() {
        User u = user("Jane", "Smith");
        Trainer t = trainer(u);
        when(trainerRepository.save(t)).thenReturn(t);

        trainerService.create(t);

        verify(userAccountService).prepareForRegistration(u);
    }

    @Test
    void createSavesAndReturnsTrainer() {
        User u = user("Jane", "Smith");
        Trainer t = trainer(u);
        when(trainerRepository.save(t)).thenReturn(t);

        assertSame(t, trainerService.create(t));
        verify(trainerRepository).save(t);
    }

    // update

    @Test
    void updateThrowsForNullTrainer() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.update(null));
        verifyNoInteractions(trainerRepository, userAccountService);
    }

    @Test
    void updateCallsValidateUserOnUser() {
        User u = user("Jane", "Smith");
        Trainer t = trainer(u);
        when(trainerRepository.save(t)).thenReturn(t);

        trainerService.update(t);

        verify(userAccountService).validateUser(u);
    }

    @Test
    void updateDoesNotCallPrepareForRegistration() {
        User u = user("Jane", "Smith");
        Trainer t = trainer(u);
        when(trainerRepository.save(t)).thenReturn(t);

        trainerService.update(t);

        verify(userAccountService, never()).prepareForRegistration(any());
    }

    @Test
    void updateSavesAndReturnsTrainer() {
        User u = user("Jane", "Smith");
        Trainer t = trainer(u);
        when(trainerRepository.save(t)).thenReturn(t);

        assertSame(t, trainerService.update(t));
        verify(trainerRepository).save(t);
    }

    // find by id

    @Test
    void findByIdReturnsPresentWhenFound() {
        User u = user("Jane", "Smith");
        Trainer t = trainerWithId(u, 10L);
        when(trainerRepository.findById(10L)).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), trainerService.find(10L));
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(trainerService.find(999L).isEmpty());
    }

    // find by username

    @Test
    void findByUsernameReturnsPresentWhenFound() {
        User u = user("Jane", "Smith");
        Trainer t = trainerWithId(u, 10L);
        when(trainerRepository.findByUserUsername("Jane.Smith")).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), trainerService.find("Jane.Smith"));
    }

    @Test
    void findByUsernameReturnsEmptyWhenNotFound() {
        when(trainerRepository.findByUserUsername("missing")).thenReturn(Optional.empty());
        assertTrue(trainerService.find("missing").isEmpty());
    }

    // findAll

    @Test
    void findAllThrowsEntityNotFoundWhenAnyTrainerMissing() {
        User u = user("Jane", "Smith");
        Trainer t = trainerWithId(u, 1L);
        Set<String> usernames = Set.of("Jane.Smith", "missing.trainer");
        when(trainerRepository.findByUserUsernameIn(usernames)).thenReturn(Set.of(t));

        assertThrows(EntityNotFoundException.class, () -> trainerService.findAll(usernames));
    }

    @Test
    void findAllReturnsSingleMatchingTrainer() {
        User u = user("Jane", "Smith");
        Trainer t = trainerWithId(u, 1L);
        Set<String> usernames = Set.of("Jane.Smith");
        when(trainerRepository.findByUserUsernameIn(usernames)).thenReturn(Set.of(t));

        Set<Trainer> result = trainerService.findAll(usernames);

        assertEquals(1, result.size());
        assertTrue(result.contains(t));
    }

    @Test
    void findAllReturnsMultipleMatchingTrainers() {
        User u1 = user("Jane", "Smith");
        User u2 = user("Bob", "Jones");
        Trainer t1 = trainerWithId(u1, 1L);
        Trainer t2 = trainerWithId(u2, 2L);
        Set<String> usernames = Set.of("Jane.Smith", "Bob.Jones");
        when(trainerRepository.findByUserUsernameIn(usernames)).thenReturn(Set.of(t1, t2));

        Set<Trainer> result = trainerService.findAll(usernames);

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
    }

    // getNotAssignedTrainers

    @Test
    void getNotAssignedTrainersDelegatesToRepository() {
        when(trainerRepository.findTrainersNotAssignedToTrainee("John.Smith")).thenReturn(Set.of());

        trainerService.getNotAssignedTrainers("John.Smith");

        verify(trainerRepository).findTrainersNotAssignedToTrainee("John.Smith");
    }

    @Test
    void getNotAssignedTrainersReturnsResultFromRepository() {
        User u = user("Jane", "Doe");
        Trainer t = trainerWithId(u, 99L);
        when(trainerRepository.findTrainersNotAssignedToTrainee("John.Smith")).thenReturn(Set.of(t));

        Set<Trainer> result = trainerService.getNotAssignedTrainers("John.Smith");

        assertEquals(1, result.size());
        assertTrue(result.contains(t));
    }
}
