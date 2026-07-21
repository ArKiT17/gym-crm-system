package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.service.TraineeService;
import com.maxlikarenko.gymcrmsystem.service.TrainerService;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(Trainee trainee) {
        log.info("Facade request to create trainee");
        return traineeService.create(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        log.info("Facade request to update trainee with id {}", trainee == null ? null : trainee.getId());
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long id) {
        log.info("Facade request to delete trainee with id {}", id);
        traineeService.delete(id);
    }

    public Optional<Trainee> findTraineeById(Long id) {
        log.debug("Facade request to find trainee with id {}", id);
        return traineeService.findById(id);
    }

    public Trainer createTrainer(Trainer trainer) {
        log.info("Facade request to create trainer");
        return trainerService.create(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        log.info("Facade request to update trainer with id {}", trainer == null ? null : trainer.getId());
        return trainerService.update(trainer);
    }

    public Optional<Trainer> findTrainerById(Long id) {
        log.debug("Facade request to find trainer with id {}", id);
        return trainerService.findById(id);
    }

    public Training createTraining(Training training) {
        log.info("Facade request to create training");
        return trainingService.create(training);
    }

    public Optional<Training> findTrainingById(Long id) {
        log.debug("Facade request to find training with id {}", id);
        return trainingService.findById(id);
    }
}
