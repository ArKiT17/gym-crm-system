package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.account.RegistrationCredentials;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerProfileUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerTrainingsQuery;
import com.maxlikarenko.gymcrmsystem.dto.response.CredentialsResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerTrainingResponse;
import com.maxlikarenko.gymcrmsystem.mapper.TrainerMapper;
import com.maxlikarenko.gymcrmsystem.mapper.TrainingMapper;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.service.TrainerService;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import com.maxlikarenko.gymcrmsystem.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class TrainerFacadeTest {
    private TrainerService trainerService;
    private TrainingService trainingService;
    private TrainingTypeService trainingTypeService;
    private TrainerMapper trainerMapper;
    private TrainingMapper trainingMapper;
    private TrainerFacade trainerFacade;

    @BeforeEach
    void setUp() {
        trainerService = mock(TrainerService.class);
        trainingService = mock(TrainingService.class);
        trainingTypeService = mock(TrainingTypeService.class);
        trainerMapper = mock(TrainerMapper.class);
        trainingMapper = mock(TrainingMapper.class);
        trainerFacade = new TrainerFacade(
                trainerService, trainingService, trainingTypeService,
                trainerMapper, trainingMapper
        );
    }

    @Test
    void createResolvesSpecializationMapsAndReturnsCredentials() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Jane", "Doe", 7L);
        TrainingType specialization = new TrainingType("yoga");
        Trainer trainer = trainer("Jane.Doe", "generated");
        when(trainingTypeService.get(7L)).thenReturn(specialization);
        when(trainerMapper.toEntity(request, specialization)).thenReturn(trainer);
        when(trainerService.create(trainer)).thenReturn(new RegistrationCredentials("Jane.Doe", "generated"));

        CredentialsResponse result = trainerFacade.create(request);

        assertEquals("Jane.Doe", result.username());
        assertEquals("generated", result.password());
        verify(trainingTypeService).get(7L);
        verify(trainerMapper).toEntity(request, specialization);
        verify(trainerService).create(trainer);
    }

    @Test
    void updateDelegatesRequestFieldsAndMapsResponse() {
        TrainerProfileUpdateRequest request = new TrainerProfileUpdateRequest("Janet", "Doe", false);
        Trainer trainer = trainer("Jane.Doe", "password");
        TrainerProfileResponse response = mock(TrainerProfileResponse.class);
        when(trainerService.update("Jane.Doe", "Janet", "Doe", false)).thenReturn(trainer);
        when(trainerMapper.toProfile(trainer)).thenReturn(response);

        assertSame(response, trainerFacade.update("Jane.Doe", request));

        verify(trainerService).update("Jane.Doe", "Janet", "Doe", false);
        verify(trainerMapper).toProfile(trainer);
    }

    @Test
    void getByIdDelegatesAndMapsProfile() {
        Trainer trainer = trainer("Jane.Doe", "password");
        TrainerProfileResponse response = mock(TrainerProfileResponse.class);
        when(trainerService.get(1L)).thenReturn(trainer);
        when(trainerMapper.toProfile(trainer)).thenReturn(response);

        assertSame(response, trainerFacade.getById(1L));

        verify(trainerService).get(1L);
        verify(trainerMapper).toProfile(trainer);
    }

    @Test
    void getByUsernameDelegatesAndMapsProfile() {
        Trainer trainer = trainer("Jane.Doe", "password");
        TrainerProfileResponse response = mock(TrainerProfileResponse.class);
        when(trainerService.get("Jane.Doe")).thenReturn(trainer);
        when(trainerMapper.toProfile(trainer)).thenReturn(response);

        assertSame(response, trainerFacade.getByUsername("Jane.Doe"));

        verify(trainerService).get("Jane.Doe");
        verify(trainerMapper).toProfile(trainer);
    }

    @Test
    void getTrainingsPassesAllFiltersAndMapsResults() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        TrainerTrainingsQuery query = new TrainerTrainingsQuery(from, to, "John.Smith");
        Training training = mock(Training.class);
        TrainerTrainingResponse response = mock(TrainerTrainingResponse.class);
        when(trainingService.getTrainerTrainings("Jane.Doe", from, to, "John.Smith"))
                .thenReturn(Set.of(training));
        when(trainingMapper.toTrainerResponse(training)).thenReturn(response);

        assertEquals(Set.of(response), trainerFacade.getTrainings("Jane.Doe", query));

        verify(trainingService).getTrainerTrainings("Jane.Doe", from, to, "John.Smith");
        verify(trainingMapper).toTrainerResponse(training);
    }

    private Trainer trainer(String username, String password) {
        User user = new User("Jane", "Doe");
        user.setUsername(username);
        user.setPassword(password);
        return new Trainer(user, new TrainingType("yoga"));
    }
}
