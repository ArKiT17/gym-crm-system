package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class TrainingService {
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingTypeService trainingTypeService;
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

    @Autowired
    public void setTrainingTypeService(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername,
                                String trainingName, String trainingTypeName,
                                LocalDate date, int duration) {

        if (isBlank(traineeUsername) || isBlank(trainerUsername) || isBlank(trainingName)
                || isBlank(trainingTypeName) || date == null || duration <= 0) {
            throw new IllegalArgumentException("Training trainee, trainer, name, type, date and positive duration are required");
        }

        Trainee trainee = traineeService.find(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeUsername));
        Trainer trainer = trainerService.find(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUsername));
        TrainingType type = trainingTypeService.find(trainingTypeName)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + trainingTypeName));

        Training training = new Training(trainee, trainer, trainingName, type, date, duration);

        log.info("Creating training '{}'", training.getName());
        return trainingRepository.save(training);
    }

    public Optional<Training> find(Long id) {
        log.debug("Finding training with id {}", id);
        return trainingRepository.findById(id);
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
