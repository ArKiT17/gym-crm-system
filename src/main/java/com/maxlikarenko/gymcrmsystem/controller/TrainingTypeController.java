package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.facade.TrainingFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Set;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training types", description = "Read-only training type catalog")
public class TrainingTypeController {
    private final TrainingFacade trainingFacade;

    public TrainingTypeController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @GetMapping
    @Operation(summary = "Get training types", description = "Returns the immutable list of available training types.")
    public ResponseEntity<Set<TrainingTypeResponse>> getAll() {
        return new ResponseEntity<>(trainingFacade.getAllTrainingTypes(), HttpStatus.OK);
    }
}
