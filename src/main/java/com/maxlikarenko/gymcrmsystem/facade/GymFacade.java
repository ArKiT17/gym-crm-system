package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class GymFacade {
    private final AuthenticationService authenticationService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final UserAccountService userAccountService;

    @Autowired
    public GymFacade(AuthenticationService authenticationService, TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, UserAccountService userAccountService) {
        this.authenticationService = authenticationService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.userAccountService = userAccountService;
    }

    public Trainee createTrainee(Trainee trainee) {
        log.info("Facade request to create trainee");
        return traineeService.create(trainee);
    }

    public Trainee updateTrainee(String authUsername, String authPassword, Trainee trainee) {
        authenticationService.authenticate(authUsername, authPassword);

        log.info("Facade request to update trainee with id {}", trainee == null ? null : trainee.getId());
        return traineeService.update(trainee);
    }

    public void deleteTrainee(String authUsername, String authPassword, Long id) {
        authenticationService.authenticate(authUsername, authPassword);

        log.info("Facade request to delete trainee with id {}", id);
        traineeService.delete(id);
    }

    public void deleteTraineeByUsername(String authUsername, String authPassword, String username) {
        authenticationService.authenticate(authUsername, authPassword);

        log.info("Facade request to delete trainee with username {}", username);
        traineeService.delete(username);
    }

    public Optional<Trainee> findTraineeById(String authUsername, String authPassword, Long id) {
        authenticationService.authenticate(authUsername, authPassword);

        log.debug("Facade request to find trainee with id {}", id);
        return traineeService.find(id);
    }

    public Optional<Trainee> findTraineeByUsername(String authUsername, String authPassword, String username) {
        authenticationService.authenticate(authUsername, authPassword);

        log.debug("Facade request to find trainee with username {}", username);
        return traineeService.find(username);
    }

    public boolean authenticateTrainee(String authUsername, String authPassword) {
        return authenticationService.authenticate(authUsername, authPassword);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        authenticationService.authenticate(username, oldPassword);

        userAccountService.changePassword(username, newPassword);
    }

    public void activateTrainee(String authUsername, String authPassword, String username) {
        authenticationService.authenticate(authUsername, authPassword);

        userAccountService.activate(username);
    }

    public void deactivateTrainee(String authUsername, String authPassword, String username) {
        authenticationService.authenticate(authUsername, authPassword);

        userAccountService.deactivate(username);
    }

    public Set<Training> getTraineeTrainings(String authUsername, String authPassword, String traineeUsername,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        authenticationService.authenticate(authUsername, authPassword);

        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    public Set<Trainer> getNotAssignedTrainers(String authUsername, String authPassword, String traineeUsername) {
        authenticationService.authenticate(authUsername, authPassword);

        return trainerService.getNotAssignedTrainers(traineeUsername);
    }

    public Set<Trainer> updateTraineeTrainers(String authUsername, String authPassword, String traineeUsername, Set<String> trainerUsernames) {
        authenticationService.authenticate(authUsername, authPassword);

        return traineeService.updateTrainers(traineeUsername, trainerUsernames);
    }

    public Trainer createTrainer(Trainer trainer) {
        log.info("Facade request to create trainer");
        return trainerService.create(trainer);
    }

    public Trainer updateTrainer(String authUsername, String authPassword, Trainer trainer) {
        authenticationService.authenticate(authUsername, authPassword);

        log.info("Facade request to update trainer with id {}", trainer == null ? null : trainer.getId());
        return trainerService.update(trainer);
    }

    public Optional<Trainer> findTrainerById(String authUsername, String authPassword, Long id) {
        authenticationService.authenticate(authUsername, authPassword);

        log.debug("Facade request to find trainer with id {}", id);
        return trainerService.find(id);
    }

    public Optional<Trainer> findTrainerByUsername(String authUsername, String authPassword, String username) {
        authenticationService.authenticate(authUsername, authPassword);

        log.debug("Facade request to find trainer with username {}", username);
        return trainerService.find(username);
    }

    public boolean authenticateTrainer(String authUsername, String authPassword) {
        return authenticationService.authenticate(authUsername, authPassword);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        authenticationService.authenticate(username, oldPassword);

        userAccountService.changePassword(username, newPassword);
    }

    public void activateTrainer(String authUsername, String authPassword, String username) {
        authenticationService.authenticate(authUsername, authPassword);

        userAccountService.activate(username);
    }

    public void deactivateTrainer(String authUsername, String authPassword, String username) {
        authenticationService.authenticate(authUsername, authPassword);

        userAccountService.deactivate(username);
    }

    public Set<Training> getTrainerTrainings(String authUsername, String authPassword, String trainerUsername,
                                              LocalDate fromDate, LocalDate toDate, String traineeName) {
        authenticationService.authenticate(authUsername, authPassword);

        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }

    public Training addTraining(String authUsername, String authPassword, String traineeUsername,
                                String trainerUsername, String trainingName, String trainingTypeName,
                                LocalDate date, int duration) {
        authenticationService.authenticate(authUsername, authPassword);

        log.info("Facade request to add training for trainee {} and trainer {}", traineeUsername, trainerUsername);
        return trainingService.addTraining(traineeUsername, trainerUsername,
                trainingName, trainingTypeName, date, duration);
    }

    public Optional<Training> findTrainingById(String authUsername, String authPassword, Long id) {
        authenticationService.authenticate(authUsername, authPassword);

        log.debug("Facade request to find training with id {}", id);
        return trainingService.find(id);
    }
}
