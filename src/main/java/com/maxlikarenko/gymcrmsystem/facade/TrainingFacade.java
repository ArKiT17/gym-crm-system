package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.dto.request.AddTrainingRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.mapper.TrainingTypeMapper;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import com.maxlikarenko.gymcrmsystem.service.TrainingTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TrainingFacade {
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final TrainingTypeMapper trainingTypeMapper;

    public TrainingFacade(TrainingService trainingService,
                          TrainingTypeService trainingTypeService,
                          TrainingTypeMapper trainingTypeMapper) {
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.trainingTypeMapper = trainingTypeMapper;
    }

    public void add(AddTrainingRequest request) {
        log.info("Facade request to add training for trainee {} and trainer {}", request.traineeUsername(), request.trainerUsername());
        trainingService.addTraining(
                request.traineeUsername(),
                request.trainerUsername(),
                request.trainingName(),
                request.trainingDate(),
                request.trainingDuration()
        );
    }

    public Set<TrainingTypeResponse> getAllTrainingTypes() {
        return trainingTypeService.getAll().stream()
                .map(trainingTypeMapper::toResponse)
                .collect(Collectors.toSet());
    }
}
