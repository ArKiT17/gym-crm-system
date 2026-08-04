package com.maxlikarenko.gymcrmsystem.mapper;

import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeTrainingResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerTrainingResponse;
import com.maxlikarenko.gymcrmsystem.model.Training;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {
    private final TrainingTypeMapper trainingTypeMapper;

    public TrainingMapper(TrainingTypeMapper trainingTypeMapper) {
        this.trainingTypeMapper = trainingTypeMapper;
    }

    public TraineeTrainingResponse toTraineeResponse(Training training) {
        return new TraineeTrainingResponse(
                training.getName(),
                training.getDate(),
                trainingTypeMapper.toResponse(training.getType()),
                training.getDuration(),
                training.getTrainer().getUser().getUsername()
        );
    }

    public TrainerTrainingResponse toTrainerResponse(Training training) {
        return new TrainerTrainingResponse(
                training.getName(),
                training.getDate(),
                trainingTypeMapper.toResponse(training.getType()),
                training.getDuration(),
                training.getTrainee().getUser().getUsername()
        );
    }
}
