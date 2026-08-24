package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.account.RegistrationCredentials;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeProfileUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeTrainersUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeTrainingsQuery;
import com.maxlikarenko.gymcrmsystem.dto.response.CredentialsResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeTrainingResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerSummaryResponse;
import com.maxlikarenko.gymcrmsystem.mapper.TraineeMapper;
import com.maxlikarenko.gymcrmsystem.mapper.TrainerMapper;
import com.maxlikarenko.gymcrmsystem.mapper.TrainingMapper;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.model.User;
import com.maxlikarenko.gymcrmsystem.service.TraineeService;
import com.maxlikarenko.gymcrmsystem.service.TrainerService;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class TraineeFacadeTest {
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;
    private TraineeMapper traineeMapper;
    private TrainerMapper trainerMapper;
    private TrainingMapper trainingMapper;
    private TraineeFacade traineeFacade;

    @BeforeEach
    void setUp() {
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingService = mock(TrainingService.class);
        traineeMapper = mock(TraineeMapper.class);
        trainerMapper = mock(TrainerMapper.class);
        trainingMapper = mock(TrainingMapper.class);
        traineeFacade = new TraineeFacade(
                traineeService, trainerService, trainingService,
                traineeMapper, trainerMapper, trainingMapper
        );
    }

    @Test
    void createMapsRequestDelegatesAndReturnsCredentials() {
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest("John", "Smith", null, "Kyiv");
        Trainee trainee = trainee("John.Smith", "generated");
        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(traineeService.create(trainee)).thenReturn(new RegistrationCredentials("John.Smith", "generated"));

        CredentialsResponse result = traineeFacade.create(request);

        assertEquals("John.Smith", result.username());
        assertEquals("generated", result.password());
        verify(traineeMapper).toEntity(request);
        verify(traineeService).create(trainee);
    }

    @Test
    void updateDelegatesRequestFieldsAndMapsResponse() {
        LocalDate birthDate = LocalDate.of(2000, 1, 1);
        TraineeProfileUpdateRequest request =
                new TraineeProfileUpdateRequest("Johnny", "Doe", birthDate, "Kyiv", false);
        Trainee trainee = trainee("John.Smith", "password");
        TraineeProfileResponse response = mock(TraineeProfileResponse.class);
        when(traineeService.update("John.Smith", "Johnny", "Doe", birthDate, "Kyiv", false))
                .thenReturn(trainee);
        when(traineeMapper.toProfile(trainee)).thenReturn(response);

        assertSame(response, traineeFacade.update("John.Smith", request));

        verify(traineeService).update("John.Smith", "Johnny", "Doe", birthDate, "Kyiv", false);
        verify(traineeMapper).toProfile(trainee);
    }

    @Test
    void deleteByIdDelegatesToService() {
        traineeFacade.delete(1L);

        verify(traineeService).delete(1L);
    }

    @Test
    void deleteByUsernameDelegatesToService() {
        traineeFacade.delete("John.Smith");

        verify(traineeService).delete("John.Smith");
    }

    @Test
    void getByIdDelegatesAndMapsProfile() {
        Trainee trainee = trainee("John.Smith", "password");
        TraineeProfileResponse response = mock(TraineeProfileResponse.class);
        when(traineeService.get(1L)).thenReturn(trainee);
        when(traineeMapper.toProfile(trainee)).thenReturn(response);

        assertSame(response, traineeFacade.getById(1L));

        verify(traineeService).get(1L);
        verify(traineeMapper).toProfile(trainee);
    }

    @Test
    void getByUsernameDelegatesAndMapsProfile() {
        Trainee trainee = trainee("John.Smith", "password");
        TraineeProfileResponse response = mock(TraineeProfileResponse.class);
        when(traineeService.get("John.Smith")).thenReturn(trainee);
        when(traineeMapper.toProfile(trainee)).thenReturn(response);

        assertSame(response, traineeFacade.getByUsername("John.Smith"));

        verify(traineeService).get("John.Smith");
        verify(traineeMapper).toProfile(trainee);
    }

    @Test
    void getTrainingsPassesAllFiltersAndMapsResults() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        TraineeTrainingsQuery query = new TraineeTrainingsQuery(from, to, "Jane.Doe", "yoga");
        Training training = mock(Training.class);
        TraineeTrainingResponse response = mock(TraineeTrainingResponse.class);
        when(trainingService.getTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga"))
                .thenReturn(Set.of(training));
        when(trainingMapper.toTraineeResponse(training)).thenReturn(response);

        assertEquals(Set.of(response), traineeFacade.getTrainings("John.Smith", query));

        verify(trainingService).getTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga");
        verify(trainingMapper).toTraineeResponse(training);
    }

    @Test
    void getNotAssignedTrainersMapsServiceResults() {
        Trainer trainer = mock(Trainer.class);
        TrainerSummaryResponse response = mock(TrainerSummaryResponse.class);
        when(trainerService.getNotAssignedTrainers("John.Smith")).thenReturn(Set.of(trainer));
        when(trainerMapper.toSummary(trainer)).thenReturn(response);

        assertEquals(Set.of(response), traineeFacade.getNotAssignedTrainers("John.Smith"));

        verify(trainerService).getNotAssignedTrainers("John.Smith");
        verify(trainerMapper).toSummary(trainer);
    }

    @Test
    void updateTrainersPassesUsernamesAndMapsResults() {
        TraineeTrainersUpdateRequest request =
                new TraineeTrainersUpdateRequest(Set.of("Jane.Doe"));
        Trainer trainer = mock(Trainer.class);
        TrainerSummaryResponse response = mock(TrainerSummaryResponse.class);
        when(traineeService.updateTrainers("John.Smith", Set.of("Jane.Doe")))
                .thenReturn(Set.of(trainer));
        when(trainerMapper.toSummary(trainer)).thenReturn(response);

        assertEquals(Set.of(response), traineeFacade.updateTrainers("John.Smith", request));

        verify(traineeService).updateTrainers("John.Smith", Set.of("Jane.Doe"));
        verify(trainerMapper).toSummary(trainer);
    }

    private Trainee trainee(String username, String password) {
        User user = new User("John", "Smith");
        user.setUsername(username);
        user.setPassword(password);
        return new Trainee(user, null, null);
    }
}
