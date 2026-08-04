package com.maxlikarenko.gymcrmsystem.dto.response.trainer;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;

import java.time.LocalDate;

public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeResponse trainingType,
        int trainingDuration,
        String traineeName
) {

}
