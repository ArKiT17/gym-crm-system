package com.maxlikarenko.gymcrmsystem.facade;

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
import com.maxlikarenko.gymcrmsystem.service.TraineeService;
import com.maxlikarenko.gymcrmsystem.service.TrainerService;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import com.maxlikarenko.gymcrmsystem.account.RegistrationCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TraineeFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    public TraineeFacade(TraineeService traineeService,
                         TrainerService trainerService,
                         TrainingService trainingService,
                         TraineeMapper traineeMapper,
                         TrainerMapper trainerMapper,
                         TrainingMapper trainingMapper) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
    }

    public CredentialsResponse create(TraineeRegistrationRequest request) {
        log.info("Facade request to create trainee");
        RegistrationCredentials credentials = traineeService.create(traineeMapper.toEntity(request));
        return new CredentialsResponse(credentials.username(), credentials.rawPassword());
    }

    public TraineeProfileResponse update(String username, TraineeProfileUpdateRequest request) {
        log.info("Facade request to update trainee with username {}", username);
        Trainee updated = traineeService.update(username, request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address(), request.isActive());
        return traineeMapper.toProfile(updated);
    }

    public void delete(Long id) {
        log.info("Facade request to delete trainee with id {}", id);
        traineeService.delete(id);
    }

    public void delete(String username) {
        log.info("Facade request to delete trainee with username {}", username);
        traineeService.delete(username);
    }

    public TraineeProfileResponse getById(Long id) {
        log.debug("Facade request to get trainee with id {}", id);
        Trainee trainee = traineeService.get(id);
        return traineeMapper.toProfile(trainee);
    }

    public TraineeProfileResponse getByUsername(String username) {
        log.debug("Facade request to get trainee with username {}", username);
        Trainee trainee = traineeService.get(username);
        return traineeMapper.toProfile(trainee);
    }

    public Set<TraineeTrainingResponse> getTrainings(String traineeUsername, TraineeTrainingsQuery query) {
        return trainingService.getTraineeTrainings(
                        traineeUsername, query.periodFrom(), query.periodTo(), query.trainerName(), query.trainingType()
                ).stream()
                .map(trainingMapper::toTraineeResponse)
                .collect(Collectors.toSet());
    }

    public Set<TrainerSummaryResponse> getNotAssignedTrainers(String traineeUsername) {
        return trainerService.getNotAssignedTrainers(traineeUsername).stream()
                .map(trainerMapper::toSummary)
                .collect(Collectors.toSet());
    }

    public Set<TrainerSummaryResponse> updateTrainers(String traineeUsername, TraineeTrainersUpdateRequest request) {
        return traineeService.updateTrainers(traineeUsername, request.trainerUsernames()).stream()
                .map(trainerMapper::toSummary)
                .collect(Collectors.toSet());
    }
}
