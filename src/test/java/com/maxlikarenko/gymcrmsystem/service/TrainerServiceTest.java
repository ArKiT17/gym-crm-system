package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.repository.TrainerRepository;
import com.maxlikarenko.gymcrmsystem.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TrainerServiceTest {

    private TrainerRepository trainerRepository;
    private PasswordGenerator passwordGenerator;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerRepository = mock(TrainerRepository.class);
        passwordGenerator = mock(PasswordGenerator.class);
        trainerService = new TrainerService();
        trainerService.setTrainerRepository(trainerRepository);
        trainerService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createRejectsNullTrainer() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.create(null));

        verifyNoInteractions(trainerRepository, passwordGenerator);
    }

    @Test
    void createRejectsTrainerWithoutFirstName() {
        Trainer trainer = Trainer.builder().lastName("Smith").build();

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));

        verifyNoInteractions(trainerRepository, passwordGenerator);
    }

    @Test
    void createRejectsTrainerWithoutLastName() {
        Trainer trainer = Trainer.builder().firstName("Jane").build();

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));

        verifyNoInteractions(trainerRepository, passwordGenerator);
    }

    @Test
    void createSetsUsernameAndPasswordAndSavesTrainer() {
        Trainer trainer = Trainer.builder()
                .id(10L)
                .firstName("Jane")
                .lastName("Smith")
                .build();
        when(trainerRepository.existsByUsername("Jane.Smith")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Password01");
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer result = trainerService.create(trainer);

        assertSame(trainer, result);
        assertEquals("Jane.Smith", trainer.getUsername());
        assertEquals("Password01", trainer.getPassword());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void createAddsSuffixWhenUsernameAlreadyExists() {
        Trainer trainer = Trainer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();
        when(trainerRepository.existsByUsername("Jane.Smith")).thenReturn(true);
        when(trainerRepository.existsByUsername("Jane.Smith1")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Password01");
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.create(trainer);

        assertEquals("Jane.Smith1", trainer.getUsername());
    }

    @Test
    void updateRejectsNullTrainer() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.update(null));

        verifyNoInteractions(trainerRepository, passwordGenerator);
    }

    @Test
    void updateSavesTrainerWithoutRegeneratingCredentials() {
        Trainer trainer = Trainer.builder()
                .id(10L)
                .firstName("Jane")
                .lastName("Smith")
                .username("Jane.Smith")
                .password("Existing01")
                .build();
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        assertSame(trainer, trainerService.update(trainer));

        verify(trainerRepository).save(trainer);
        verifyNoInteractions(passwordGenerator);
    }

    @Test
    void findByIdReturnsFoundTrainer() {
        Trainer trainer = Trainer.builder().id(10L).build();
        when(trainerRepository.findById(10L)).thenReturn(Optional.of(trainer));

        assertEquals(Optional.of(trainer), trainerService.findById(10L));
    }

    @Test
    void findByIdReturnsEmptyWhenTrainerDoesNotExist() {
        when(trainerRepository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(trainerService.findById(999L).isEmpty());
    }
}
