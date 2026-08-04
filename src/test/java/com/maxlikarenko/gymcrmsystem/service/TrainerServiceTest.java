package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void createGeneratesCredentialsAndSavesTrainer() {
        User user = new User("Jane", "Smith");
        Trainer trainer = new Trainer(user, null);
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        assertSame(trainer, trainerService.create(trainer));
        verify(userAccountService).generateCredentials(user);
        verify(trainerRepository).save(trainer);
    }

    @Test
    void createRejectsNullTrainer() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.create(null));
        verifyNoInteractions(userAccountService, trainerRepository);
    }

    @Test
    void updateChangesTrainerFieldsByUsername() {
        Trainer trainer = new Trainer(new User("Jane", "Smith"), null);
        when(trainerRepository.findByUserUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.update("Jane.Smith", "Janet", "Doe", false);

        assertSame(trainer, result);
        assertAll(
                () -> assertEquals("Janet", trainer.getUser().getFirstName()),
                () -> assertEquals("Doe", trainer.getUser().getLastName()),
                () -> assertFalse(trainer.getUser().isActive())
        );
    }

    @Test
    void getByIdAndUsernameReturnTrainer() {
        Trainer trainer = new Trainer(new User("Jane", "Smith"), null);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerRepository.findByUserUsername("Jane.Smith")).thenReturn(Optional.of(trainer));

        assertAll(
                () -> assertSame(trainer, trainerService.get(1L)),
                () -> assertSame(trainer, trainerService.get("Jane.Smith"))
        );
    }

    @Test
    void getThrowsWhenTrainerDoesNotExist() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());
        when(trainerRepository.findByUserUsername("missing")).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(EntityNotFoundException.class, () -> trainerService.get(1L)),
                () -> assertThrows(EntityNotFoundException.class, () -> trainerService.get("missing"))
        );
    }

    @Test
    void getAllRejectsMissingTrainer() {
        Trainer trainer = new Trainer(new User("Jane", "Smith"), null);
        Set<String> usernames = Set.of("Jane.Smith", "missing");
        when(trainerRepository.findByUserUsernameIn(usernames)).thenReturn(Set.of(trainer));

        assertThrows(EntityNotFoundException.class, () -> trainerService.getAll(usernames));
    }

    @Test
    void getNotAssignedTrainersDelegatesToRepository() {
        Set<Trainer> trainers = Set.of(new Trainer(new User("Jane", "Smith"), null));
        when(trainerRepository.findTrainersNotAssignedToTrainee("John.Smith")).thenReturn(trainers);

        assertSame(trainers, trainerService.getNotAssignedTrainers("John.Smith"));
    }
}
