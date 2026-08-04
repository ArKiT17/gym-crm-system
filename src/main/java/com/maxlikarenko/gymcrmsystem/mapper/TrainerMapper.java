package com.maxlikarenko.gymcrmsystem.mapper;

import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeSummaryResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerSummaryResponse;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TrainerMapper {
    public Trainer toEntity(TrainerRegistrationRequest request, TrainingType specialization) {
        return new Trainer(
                new User(request.firstName(), request.lastName()),
                specialization
        );
    }

    public TrainerProfileResponse toProfile(Trainer trainer) {
        User user = trainer.getUser();
        Set<TraineeSummaryResponse> trainees = trainer.getTrainees() == null
                ? Set.of()
                : trainer.getTrainees().stream()
                .map(this::toTraineeSummary)
                .collect(Collectors.toSet());

        return new TrainerProfileResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization() == null
                        ? null
                        : new TrainingTypeResponse(
                        trainer.getSpecialization().getId(),
                        trainer.getSpecialization().getName()),
                user.isActive(),
                trainees
        );
    }

    public TrainerSummaryResponse toSummary(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerSummaryResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization() == null
                        ? null
                        : new TrainingTypeResponse(
                        trainer.getSpecialization().getId(),
                        trainer.getSpecialization().getName())
        );
    }

    private TraineeSummaryResponse toTraineeSummary(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeSummaryResponse(user.getUsername(), user.getFirstName(), user.getLastName());
    }
}
