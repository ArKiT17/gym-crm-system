package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import com.maxlikarenko.gymcrmsystem.exception.ConflictException;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
public class TrainingService {
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingRepository trainingRepository;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername, String trainingName,
                                LocalDate date, int duration) {

        Trainee trainee = traineeService.get(traineeUsername);
        if (!trainee.getUser().isActive()) {
            throw new ConflictException("Inactive trainee cannot have trainings");
        }
        Trainer trainer = trainerService.get(trainerUsername);
        if (!trainer.getUser().isActive()) {
            throw new ConflictException("Inactive trainer cannot conduct trainings");
        }
        TrainingType trainingType = trainer.getSpecialization();
        if (trainingType == null) {
            throw new ConflictException("Trainer has no specialization");
        }

        Training training = new Training(trainee, trainer, trainingName, trainingType, date, duration);

        log.info("Creating training '{}'", training.getName());
        return trainingRepository.save(training);
    }

    public Training get(Long id) {
        log.debug("Finding training with id {}", id);
        return trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training with id " + id + " not found"));
    }

    public Set<Training> getTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                             String trainerUsername, String trainingTypeName) {
        log.debug("Getting trainings for trainee {}", traineeUsername);
        return trainingRepository.findTraineeTrainings(traineeUsername, fromDate, toDate, trainerUsername, trainingTypeName);
    }

    public Set<Training> getTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                             String traineeUsername) {
        log.debug("Getting trainings for trainer {}", trainerUsername);
        return trainingRepository.findTrainerTrainings(trainerUsername, fromDate, toDate, traineeUsername);
    }
}
