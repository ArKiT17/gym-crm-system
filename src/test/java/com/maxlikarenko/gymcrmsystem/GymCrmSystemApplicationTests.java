package com.maxlikarenko.gymcrmsystem;

import com.maxlikarenko.gymcrmsystem.model.*;
import com.maxlikarenko.gymcrmsystem.repository.*;
import com.maxlikarenko.gymcrmsystem.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GymCrmSystemApplicationTests {

    @Autowired
    private TraineeService traineeService;
    @Autowired
    private TrainerService trainerService;
    @Autowired
    private TrainingService trainingService;
    @Autowired
    private TrainingTypeService trainingTypeService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TraineeRepository traineeRepository;
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private TrainingRepository trainingRepository;

    // context

    @Test
    void contextLoads() {
        assertNotNull(traineeService);
        assertNotNull(trainerService);
        assertNotNull(trainingService);
        assertNotNull(userAccountService);
    }

    // trainee CRUD

    @Test
    void createTraineePersistsUserAndGeneratesCredentials() {
        User user = new User("John", "Smith", null, null, true);
        Trainee trainee = new Trainee(user, LocalDate.of(1990, 5, 20), "123 Main St",
                new HashSet<>(), new HashSet<>());

        Trainee created = traineeService.create(trainee);

        assertNotNull(created.getId());
        assertNotNull(created.getUser().getId());
        assertNotNull(created.getUser().getUsername());
        assertEquals(10, created.getUser().getPassword().length());
        assertTrue(traineeRepository.findById(created.getId()).isPresent());
    }

    @Test
    void createTraineeGeneratesUniqueUsernameOnCollision() {
        User u1 = new User("Alice", "Wonder", null, null, true);
        traineeService.create(new Trainee(u1, null, null, new HashSet<>(), new HashSet<>()));

        User u2 = new User("Alice", "Wonder", null, null, true);
        traineeService.create(new Trainee(u2, null, null, new HashSet<>(), new HashSet<>()));

        assertEquals("Alice.Wonder", u1.getUsername());
        assertEquals("Alice.Wonder1", u2.getUsername());
    }

    @Test
    void findTraineeByUsernameReturnsCreatedTrainee() {
        User user = new User("Find", "Me", null, null, true);
        Trainee trainee = new Trainee(user, null, null, new HashSet<>(), new HashSet<>());
        traineeService.create(trainee);
        String username = user.getUsername();

        Optional<Trainee> found = traineeService.find(username);

        assertTrue(found.isPresent());
        assertEquals(username, found.get().getUser().getUsername());
    }

    @Test
    void updateTraineePersistsAddress() {
        User user = new User("Update", "Me", null, null, true);
        Trainee trainee = new Trainee(user, null, null, new HashSet<>(), new HashSet<>());
        traineeService.create(trainee);

        trainee.setAddress("New Street 42");
        traineeService.update(trainee);

        assertEquals("New Street 42",
                traineeRepository.findById(trainee.getId()).orElseThrow().getAddress());
    }

    @Test
    void deleteTraineeCascadesDeleteToUser() {
        User user = new User("Delete", "Me", null, null, true);
        Trainee trainee = new Trainee(user, null, null, new HashSet<>(), new HashSet<>());
        traineeService.create(trainee);
        Long traineeId = trainee.getId();
        Long userId = user.getId();

        traineeService.delete(traineeId);

        assertFalse(traineeRepository.findById(traineeId).isPresent());
        assertFalse(userRepository.findById(userId).isPresent());
    }

    @Test
    void deleteTraineeByUsernameCascadesDeleteToUser() {
        User user = new User("DeleteU", "Name", null, null, true);
        Trainee trainee = new Trainee(user, null, null, new HashSet<>(), new HashSet<>());
        traineeService.create(trainee);
        String username = user.getUsername();
        Long userId = user.getId();

        traineeService.delete(username);

        assertFalse(traineeRepository.findByUserUsername(username).isPresent());
        assertFalse(userRepository.findById(userId).isPresent());
    }

    // trainer CRUD

    @Test
    void createTrainerPersistsUserAndGeneratesCredentials() {
        User user = new User("Jane", "Doe", null, null, true);
        Trainer trainer = new Trainer(user, null, new HashSet<>(), new HashSet<>());

        Trainer created = trainerService.create(trainer);

        assertNotNull(created.getId());
        assertNotNull(created.getUser().getUsername());
        assertEquals(10, created.getUser().getPassword().length());
        assertTrue(trainerRepository.findById(created.getId()).isPresent());
    }

    @Test
    void findTrainerByUsernameReturnsCreatedTrainer() {
        User user = new User("Find", "Trainer", null, null, true);
        Trainer trainer = new Trainer(user, null, new HashSet<>(), new HashSet<>());
        trainerService.create(trainer);

        Optional<Trainer> found = trainerService.find(user.getUsername());

        assertTrue(found.isPresent());
    }

    // training types seeded by initializer

    @Test
    void trainingTypeInitializerSeedsExpectedTypes() {
        assertTrue(trainingTypeService.find("fitness").isPresent());
        assertTrue(trainingTypeService.find("yoga").isPresent());
        assertTrue(trainingTypeService.find("zumba").isPresent());
        assertTrue(trainingTypeService.find("stretching").isPresent());
        assertTrue(trainingTypeService.find("resistance").isPresent());
    }

    // addTraining

    @Test
    void addTrainingPersistsTraining() {
        User tu = new User("Trainee", "One", null, null, true);
        traineeService.create(new Trainee(tu, null, null, new HashSet<>(), new HashSet<>()));

        User tru = new User("Trainer", "One", null, null, true);
        trainerService.create(new Trainer(tru, null, new HashSet<>(), new HashSet<>()));

        LocalDate date = LocalDate.of(2025, 6, 15);
        Training training = trainingService.addTraining(
                tu.getUsername(), tru.getUsername(), "Yoga Session", "yoga", date, 90);

        assertNotNull(training.getId());
        assertTrue(trainingRepository.findById(training.getId()).isPresent());
        assertEquals("Yoga Session", training.getName());
        assertEquals(90, training.getDuration());
    }

    // many-to-many trainer assignment

    @Test
    void updateTrainersAssignsManyToManyRelationship() {
        User tu = new User("Multi", "Trainee", null, null, true);
        traineeService.create(new Trainee(tu, null, null, new HashSet<>(), new HashSet<>()));

        User tru1 = new User("Trainer", "Alpha", null, null, true);
        trainerService.create(new Trainer(tru1, null, new HashSet<>(), new HashSet<>()));

        User tru2 = new User("Trainer", "Beta", null, null, true);
        trainerService.create(new Trainer(tru2, null, new HashSet<>(), new HashSet<>()));

        Set<String> trainerNames = Set.of(tru1.getUsername(), tru2.getUsername());
        Set<Trainer> assigned = traineeService.updateTrainers(tu.getUsername(), trainerNames);

        assertEquals(2, assigned.size());
    }

    @Test
    void getNotAssignedTrainersExcludesAlreadyAssignedTrainers() {
        User tu = new User("Test", "Trainee", null, null, true);
        traineeService.create(new Trainee(tu, null, null, new HashSet<>(), new HashSet<>()));

        User assigned = new User("Assigned", "Trainer", null, null, true);
        trainerService.create(new Trainer(assigned, null, new HashSet<>(), new HashSet<>()));

        User notAssigned = new User("NotAssigned", "Trainer", null, null, true);
        trainerService.create(new Trainer(notAssigned, null, new HashSet<>(), new HashSet<>()));

        traineeService.updateTrainers(tu.getUsername(), Set.of(assigned.getUsername()));

        Set<Trainer> result = trainerService.getNotAssignedTrainers(tu.getUsername());

        assertTrue(result.stream().noneMatch(t -> t.getUser().getUsername().equals(assigned.getUsername())));
        assertTrue(result.stream().anyMatch(t -> t.getUser().getUsername().equals(notAssigned.getUsername())));
    }

    // activate / deactivate

    @Test
    void activateAndDeactivateUser() {
        User user = new User("Toggle", "User", null, null, false);
        traineeService.create(new Trainee(user, null, null, new HashSet<>(), new HashSet<>()));
        String username = user.getUsername();

        userAccountService.activate(username);
        assertTrue(userRepository.findByUsername(username).orElseThrow().isActive());

        userAccountService.deactivate(username);
        assertFalse(userRepository.findByUsername(username).orElseThrow().isActive());
    }

    // password change

    @Test
    void changePasswordUpdatesUserPassword() {
        User user = new User("Pass", "Change", null, null, true);
        traineeService.create(new Trainee(user, null, null, new HashSet<>(), new HashSet<>()));
        String username = user.getUsername();

        userAccountService.changePassword(username, "BrandNewPass");

        assertEquals("BrandNewPass",
                userRepository.findByUsername(username).orElseThrow().getPassword());
    }

    // training filtering queries

    @Test
    void getTraineeTrainingsFiltersByDateRange() {
        User tu = new User("Filter", "Trainee", null, null, true);
        traineeService.create(new Trainee(tu, null, null, new HashSet<>(), new HashSet<>()));

        User tru = new User("Filter", "Trainer", null, null, true);
        trainerService.create(new Trainer(tru, null, new HashSet<>(), new HashSet<>()));

        trainingService.addTraining(tu.getUsername(), tru.getUsername(),
                "Session 1", "yoga", LocalDate.of(2025, 3, 1), 60);
        trainingService.addTraining(tu.getUsername(), tru.getUsername(),
                "Session 2", "yoga", LocalDate.of(2025, 7, 1), 60);

        Set<Training> filtered = trainingService.getTraineeTrainings(
                tu.getUsername(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 1), null, null);

        assertEquals(1, filtered.size());
        assertEquals("Session 1", filtered.iterator().next().getName());
    }
}
