package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.facade.TrainingFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/training-types")
public class TrainingTypeController {
    private final TrainingFacade trainingFacade;

    public TrainingTypeController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @GetMapping
    public ResponseEntity<Set<TrainingTypeResponse>> getAll() {
        return new ResponseEntity<>(trainingFacade.getAllTrainingTypes(), HttpStatus.OK);
    }
}
