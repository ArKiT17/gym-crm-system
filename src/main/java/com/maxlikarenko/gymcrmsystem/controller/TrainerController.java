package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerProfileUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerTrainingsQuery;
import com.maxlikarenko.gymcrmsystem.dto.response.CredentialsResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerTrainingResponse;
import com.maxlikarenko.gymcrmsystem.facade.TrainerFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {
    private final TrainerFacade trainerFacade;

    @Autowired
    public TrainerController(TrainerFacade trainerFacade) {
        this.trainerFacade = trainerFacade;
    }

    @PostMapping
    public ResponseEntity<CredentialsResponse> create(@Valid @RequestBody TrainerRegistrationRequest request) {
        return new ResponseEntity<>(trainerFacade.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> get(@PathVariable String username) {
        return new ResponseEntity<>(trainerFacade.getByUsername(username), HttpStatus.OK);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> update(@PathVariable String username,
                                                         @Valid @RequestBody TrainerProfileUpdateRequest request) {
        return new ResponseEntity<>(trainerFacade.update(username, request), HttpStatus.OK);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<Set<TrainerTrainingResponse>> trainings(@PathVariable String username,
                                                                  @Valid @ModelAttribute TrainerTrainingsQuery query) {
        return new ResponseEntity<>(trainerFacade.getTrainings(username, query), HttpStatus.OK);
    }
}
