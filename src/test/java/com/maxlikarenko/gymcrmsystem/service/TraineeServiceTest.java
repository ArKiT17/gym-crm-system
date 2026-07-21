package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
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

class TraineeServiceTest {

    private TraineeRepository traineeRepository;
    private PasswordGenerator passwordGenerator;
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeRepository = mock(TraineeRepository.class);
        passwordGenerator = mock(PasswordGenerator.class);
        traineeService = new TraineeService(traineeRepository, passwordGenerator);
    }

    @Test
    void createRejectsNullTrainee() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.create(null));

        verifyNoInteractions(traineeRepository, passwordGenerator);
    }

    @Test
    void createSetsUsernameAndPasswordAndSavesTrainee() {
        Trainee trainee = Trainee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .build();
        when(traineeRepository.existsByUsername("John.Smith")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Password01");
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        Trainee result = traineeService.create(trainee);

        assertSame(trainee, result);
        assertEquals("John.Smith", trainee.getUsername());
        assertEquals("Password01", trainee.getPassword());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void createAddsSuffixWhenUsernameAlreadyExists() {
        Trainee trainee = Trainee.builder()
                .firstName("John")
                .lastName("Smith")
                .build();
        when(traineeRepository.existsByUsername("John.Smith")).thenReturn(true);
        when(traineeRepository.existsByUsername("John.Smith1")).thenReturn(false);
        when(passwordGenerator.generate()).thenReturn("Password01");
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        traineeService.create(trainee);

        assertEquals("John.Smith1", trainee.getUsername());
    }

    @Test
    void updateRejectsNullTrainee() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.update(null));

        verifyNoInteractions(traineeRepository, passwordGenerator);
    }

    @Test
    void updateSavesTraineeWithoutRegeneratingCredentials() {
        Trainee trainee = Trainee.builder()
                .id(1L)
                .username("John.Smith")
                .password("Existing01")
                .build();
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        assertSame(trainee, traineeService.update(trainee));

        verify(traineeRepository).save(trainee);
        verifyNoInteractions(passwordGenerator);
    }

    @Test
    void deleteDelegatesToRepository() {
        traineeService.delete(1L);

        verify(traineeRepository).deleteById(1L);
    }

    @Test
    void findByIdReturnsFoundTrainee() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.findById(1L);

        assertEquals(Optional.of(trainee), result);
    }

    @Test
    void findByIdReturnsEmptyWhenTraineeDoesNotExist() {
        when(traineeRepository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(traineeService.findById(999L).isEmpty());
    }
}
