package com.maxlikarenko.gymcrmsystem.dto.response.trainee;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeResponse trainingType,
        int trainingDuration,
        String trainerName
) {

}