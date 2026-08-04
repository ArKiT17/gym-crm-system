package com.maxlikarenko.gymcrmsystem.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserActivationRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {

}
