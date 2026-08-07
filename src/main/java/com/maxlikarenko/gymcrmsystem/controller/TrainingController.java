package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.request.AddTrainingRequest;
import com.maxlikarenko.gymcrmsystem.facade.TrainingFacade;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Trainings", description = "Training management endpoints")
public class TrainingController {
    private final TrainingFacade trainingFacade;

    public TrainingController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @PostMapping
    @Operation(summary = "Add training", description = "Adds a training for an existing trainee and trainer.")
    public ResponseEntity<Void> add(@Valid @RequestBody AddTrainingRequest request) {
        trainingFacade.add(request);
        return ResponseEntity.ok().build();
    }
}
