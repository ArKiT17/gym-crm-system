package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerProfileUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerTrainingsQuery;
import com.maxlikarenko.gymcrmsystem.dto.response.CredentialsResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerTrainingResponse;
import com.maxlikarenko.gymcrmsystem.mapper.TrainerMapper;
import com.maxlikarenko.gymcrmsystem.mapper.TrainingMapper;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.service.TrainerService;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import com.maxlikarenko.gymcrmsystem.service.TrainingTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TrainerFacade {
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    @Autowired
    public TrainerFacade(TrainerService trainerService,
                         TrainingService trainingService,
                         TrainingTypeService trainingTypeService,
                         TrainerMapper trainerMapper,
                         TrainingMapper trainingMapper) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
    }

    public CredentialsResponse create(TrainerRegistrationRequest request) {
        log.info("Facade request to create trainer");
        TrainingType specialization = trainingTypeService.get(request.specializationId());
        Trainer created = trainerService.create(trainerMapper.toEntity(request, specialization));
        return new CredentialsResponse(created.getUser().getUsername(), created.getUser().getPassword());
    }

    public TrainerProfileResponse update(String username, TrainerProfileUpdateRequest request) {
        log.info("Facade request to update trainer with username {}", username);
        Trainer updated = trainerService.update(username, request.firstName(), request.lastName(), request.isActive());
        return trainerMapper.toProfile(updated);
    }

    public TrainerProfileResponse getById(Long id) {
        log.debug("Facade request to find trainer with id {}", id);
        Trainer trainer = trainerService.get(id);
        return trainerMapper.toProfile(trainer);
    }

    public TrainerProfileResponse getByUsername(String username) {
        log.debug("Facade request to find trainer with username {}", username);
        Trainer trainer = trainerService.get(username);
        return trainerMapper.toProfile(trainer);
    }

    public Set<TrainerTrainingResponse> getTrainings(String trainerUsername, TrainerTrainingsQuery query) {
        return trainingService.getTrainerTrainings(
                        trainerUsername, query.periodFrom(), query.periodTo(), query.traineeName()
                ).stream()
                .map(trainingMapper::toTrainerResponse)
                .collect(Collectors.toSet());
    }
}
