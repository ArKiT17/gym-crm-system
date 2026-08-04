package com.maxlikarenko.gymcrmsystem.dto.request.trainee;

import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

public record TraineeTrainingsQuery(
        LocalDate periodFrom,
        LocalDate periodTo,
        String trainerName,
        String trainingType
) {
    @AssertTrue(message = "Period from must not be after period to")
    public boolean isDateRangeValid() {
        return periodFrom == null || periodTo == null || !periodFrom.isAfter(periodTo);
    }
}
