package com.maxlikarenko.gymcrmsystem.mapper;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeMapper {
    public TrainingTypeResponse toResponse(TrainingType trainingType) {
        return trainingType == null
                ? null
                : new TrainingTypeResponse(trainingType.getId(), trainingType.getName());
    }
}
