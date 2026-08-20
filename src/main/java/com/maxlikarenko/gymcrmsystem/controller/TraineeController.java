package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeProfileUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeTrainersUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeTrainingsQuery;
import com.maxlikarenko.gymcrmsystem.dto.response.CredentialsResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeTrainingResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerSummaryResponse;
import com.maxlikarenko.gymcrmsystem.facade.TraineeFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainees", description = "Trainee registration, profiles, trainers, and trainings")
public class TraineeController {
    private final TraineeFacade traineeFacade;

    public TraineeController(TraineeFacade traineeFacade) {
        this.traineeFacade = traineeFacade;
    }

    @PostMapping
    @Operation(summary = "Register trainee", description = "Creates a trainee profile and returns generated credentials.")
    public ResponseEntity<CredentialsResponse> create(@Valid @RequestBody TraineeRegistrationRequest request) {
        return new ResponseEntity<>(traineeFacade.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile")
    public ResponseEntity<TraineeProfileResponse> get(@PathVariable String username) {
        return new ResponseEntity<>(traineeFacade.getByUsername(username), HttpStatus.OK);
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> update(@PathVariable String username,
                                                         @Valid @RequestBody TraineeProfileUpdateRequest request) {
        return new ResponseEntity<>(traineeFacade.update(username, request), HttpStatus.OK);
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile", description = "Hard-deletes the trainee and related trainings.")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        traineeFacade.delete(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/available-trainers")
    @Operation(summary = "Get available trainers")
    public ResponseEntity<Set<TrainerSummaryResponse>> availableTrainers(@PathVariable String username) {
        return new ResponseEntity<>(traineeFacade.getNotAssignedTrainers(username), HttpStatus.OK);
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee trainer list")
    public ResponseEntity<Set<TrainerSummaryResponse>> updateTrainers(@PathVariable String username,
                                                                      @Valid @RequestBody TraineeTrainersUpdateRequest request) {
        return new ResponseEntity<>(traineeFacade.updateTrainers(username, request), HttpStatus.OK);
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee trainings")
    public ResponseEntity<Set<TraineeTrainingResponse>> trainings(@PathVariable String username,
                                                                  @Valid @ModelAttribute TraineeTrainingsQuery query) {
        return new ResponseEntity<>(traineeFacade.getTrainings(username, query), HttpStatus.OK);
    }
}
