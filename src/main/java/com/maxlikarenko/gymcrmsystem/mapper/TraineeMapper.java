package com.maxlikarenko.gymcrmsystem.mapper;

import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeSummaryResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerSummaryResponse;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TraineeMapper {
    public Trainee toEntity(TraineeRegistrationRequest request) {
        return new Trainee(
                new User(request.firstName(), request.lastName()),
                request.dateOfBirth(),
                request.address()
        );
    }

    public TraineeProfileResponse toProfile(Trainee trainee) {
        User user = trainee.getUser();
        Set<TrainerSummaryResponse> trainers = trainee.getTrainers() == null
                ? Set.of()
                : trainee.getTrainers().stream()
                        .map(this::toTrainerSummary)
                        .collect(Collectors.toSet());

        return new TraineeProfileResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                user.isActive(),
                trainers
        );
    }

    public TraineeSummaryResponse toSummary(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeSummaryResponse(user.getUsername(), user.getFirstName(), user.getLastName());
    }

    private TrainerSummaryResponse toTrainerSummary(Trainer trainer) {
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
}
