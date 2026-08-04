package com.maxlikarenko.gymcrmsystem.dto.request.trainer;

import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

public record TrainerTrainingsQuery(
        LocalDate periodFrom,
        LocalDate periodTo,
        String traineeName
) {
    @AssertTrue(message = "Period from must not be after period to")
    public boolean isDateRangeValid() {
        return periodFrom == null || periodTo == null || !periodFrom.isAfter(periodTo);
    }
}
