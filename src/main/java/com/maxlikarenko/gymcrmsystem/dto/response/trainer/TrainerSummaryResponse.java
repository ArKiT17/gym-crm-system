package com.maxlikarenko.gymcrmsystem.dto.response.trainer;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;

public record TrainerSummaryResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization
) {

}
