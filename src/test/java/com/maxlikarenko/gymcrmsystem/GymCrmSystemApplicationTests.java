package com.maxlikarenko.gymcrmsystem;

import com.maxlikarenko.gymcrmsystem.facade.GymFacade;
import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import com.maxlikarenko.gymcrmsystem.service.TraineeService;
import com.maxlikarenko.gymcrmsystem.service.TrainerService;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GymCrmSystemApplicationTests {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    @Qualifier("traineeStorage")
    private Map<Long, Trainee> traineeStorage;

    @Autowired
    @Qualifier("trainerStorage")
    private Map<Long, Trainer> trainerStorage;

    @Autowired
    @Qualifier("trainingStorage")
    private Map<Long, Training> trainingStorage;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainingService trainingService;

    @AfterEach
    void cleanUpCreatedProfiles() {
        traineeStorage.remove(900L);
        trainerStorage.remove(901L);
        trainingStorage.remove(902L);
    }

    @Test
    void contextLoadsRequiredBeansAndInitializesStorage() {
        assertNotNull(gymFacade);
        assertEquals(5, traineeStorage.size());
        assertEquals(5, trainerStorage.size());
        assertEquals(5, trainingStorage.size());
    }

    @Test
    void servicesUseAutowiredDaosAndPersistTraineeProfile() {
        Trainee trainee = Trainee.builder()
                .id(900L)
                .firstName("Integration")
                .lastName("Trainee")
                .build();

        Trainee created = traineeService.create(trainee);

        assertEquals("Integration.Trainee", created.getUsername());
        assertNotNull(created.getPassword());
        assertEquals(10, created.getPassword().length());
        assertEquals(created, traineeService.findById(900L).orElseThrow());

        created.setAddress("Updated address");
        traineeService.update(created);
        assertEquals("Updated address", traineeService.findById(900L).orElseThrow().getAddress());

        traineeService.delete(900L);
        assertTrue(traineeService.findById(900L).isEmpty());
    }

    @Test
    void servicesUseAutowiredDaosAndPersistTrainerAndTrainingProfiles() {
        Trainer trainer = Trainer.builder()
                .id(901L)
                .firstName("Integration")
                .lastName("Trainer")
                .build();
        Training training = Training.builder()
                .id(902L)
                .name("Integration Training")
                .trainer(trainer)
                .build();

        Trainer createdTrainer = trainerService.create(trainer);
        Training createdTraining = trainingService.create(training);

        assertEquals("Integration.Trainer", createdTrainer.getUsername());
        assertNotNull(createdTrainer.getPassword());
        assertEquals(10, createdTrainer.getPassword().length());
        assertEquals(createdTrainer, trainerService.findById(901L).orElseThrow());
        assertEquals(createdTraining, trainingService.findById(902L).orElseThrow());
    }

}
