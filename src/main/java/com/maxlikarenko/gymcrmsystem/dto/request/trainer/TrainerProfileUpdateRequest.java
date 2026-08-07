package com.maxlikarenko.gymcrmsystem.dto.request.trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerProfileUpdateRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotNull(message = "Active status is required")
        boolean isActive
) {

}
