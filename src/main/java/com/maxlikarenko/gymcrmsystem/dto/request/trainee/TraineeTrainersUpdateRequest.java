package com.maxlikarenko.gymcrmsystem.dto.request.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record TraineeTrainersUpdateRequest(
        @NotEmpty(message = "At least one trainer is required")
        Set<@NotBlank(message = "Trainer username is required") String> trainerUsernames
) {

}
