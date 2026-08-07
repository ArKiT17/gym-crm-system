package com.maxlikarenko.gymcrmsystem.dto.response.trainee;

import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerSummaryResponse;

import java.time.LocalDate;
import java.util.Set;

public record TraineeProfileResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive,
        Set<TrainerSummaryResponse> trainers
) {
    public TraineeProfileResponse(String username, String firstName, String lastName,
                                  LocalDate dateOfBirth, String address, boolean isActive,
                                  Set<TrainerSummaryResponse> trainers) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.isActive = isActive;
        this.trainers = trainers == null ? Set.of() : Set.copyOf(trainers);
    }
}
