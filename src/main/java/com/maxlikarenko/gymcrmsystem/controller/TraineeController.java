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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {
    private final TraineeFacade traineeFacade;

    @Autowired
    public TraineeController(TraineeFacade traineeFacade) {
        this.traineeFacade = traineeFacade;
    }

    @PostMapping
    public ResponseEntity<CredentialsResponse> create(@Valid @RequestBody TraineeRegistrationRequest request) {
        return new ResponseEntity<>(traineeFacade.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> get(@PathVariable String username) {
        return new ResponseEntity<>(traineeFacade.getByUsername(username), HttpStatus.OK);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> update(@PathVariable String username,
                                                         @Valid @RequestBody TraineeProfileUpdateRequest request) {
        return new ResponseEntity<>(traineeFacade.update(username, request), HttpStatus.OK);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        traineeFacade.delete(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/available-trainers")
    public ResponseEntity<Set<TrainerSummaryResponse>> availableTrainers(@PathVariable String username) {
        return new ResponseEntity<>(traineeFacade.getNotAssignedTrainers(username), HttpStatus.OK);
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<Set<TrainerSummaryResponse>> updateTrainers(@PathVariable String username,
                                                                      @Valid @RequestBody TraineeTrainersUpdateRequest request) {
        return new ResponseEntity<>(traineeFacade.updateTrainers(username, request), HttpStatus.OK);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<Set<TraineeTrainingResponse>> trainings(@PathVariable String username,
                                                                  @Valid @ModelAttribute TraineeTrainingsQuery query) {
        return new ResponseEntity<>(traineeFacade.getTrainings(username, query), HttpStatus.OK);
    }
}
