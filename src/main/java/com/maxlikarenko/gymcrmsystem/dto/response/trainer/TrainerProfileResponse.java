package com.maxlikarenko.gymcrmsystem.dto.response.trainer;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeSummaryResponse;

import java.util.Set;

public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization,
        boolean isActive,
        Set<TraineeSummaryResponse> trainees
) {
    public TrainerProfileResponse(String username, String firstName, String lastName,
                                  TrainingTypeResponse specialization, boolean isActive,
                                  Set<TraineeSummaryResponse> trainees) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.isActive = isActive;
        this.trainees = trainees == null ? Set.of() : Set.copyOf(trainees);
    }
}