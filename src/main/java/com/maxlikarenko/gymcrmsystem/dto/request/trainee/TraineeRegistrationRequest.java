package com.maxlikarenko.gymcrmsystem.dto.request.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record TraineeRegistrationRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth,

        String address
) {
    
}
