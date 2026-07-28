package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.model.*;
import com.maxlikarenko.gymcrmsystem.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GymFacadeTest {

    private AuthenticationService authenticationService;
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;
    private UserAccountService userAccountService;
    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingService = mock(TrainingService.class);
        userAccountService = mock(UserAccountService.class);
        gymFacade = new GymFacade(authenticationService, traineeService, trainerService,
                trainingService, userAccountService);
    }

    private User user(String first, String last) {
        return new User(first, last, first + "." + last, "pass", true);
    }

    private Trainee trainee(User u) {
        return new Trainee(u, null, null, new HashSet<>(), new HashSet<>());
    }

    private Trainer trainer(User u) {
        return new Trainer(u, null, new HashSet<>(), new HashSet<>());
    }

    // createTrainee — no auth required

    @Test
    void createTraineeDelegatesToTraineeServiceWithoutAuth() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(traineeService.create(t)).thenReturn(t);

        assertSame(t, gymFacade.createTrainee(t));
        verify(traineeService).create(t);
        verifyNoInteractions(authenticationService);
    }

    // updateTrainee — auth required

    @Test
    void updateTraineeAuthenticatesBeforeUpdating() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);
        when(traineeService.update(t)).thenReturn(t);

        assertSame(t, gymFacade.updateTrainee("John.Smith", "pass", t));
        verify(authenticationService).authenticate("John.Smith", "pass");
        verify(traineeService).update(t);
    }

    @Test
    void updateTraineeThrowsAndSkipsUpdateWhenAuthFails() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(authenticationService.authenticate("John.Smith", "wrong"))
                .thenThrow(new IllegalArgumentException("auth failed"));

        assertThrows(IllegalArgumentException.class,
                () -> gymFacade.updateTrainee("John.Smith", "wrong", t));
        verifyNoInteractions(traineeService);
    }

    // deleteTrainee — auth required

    @Test
    void deleteTraineeDelegatesToTraineeServiceAfterAuth() {
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);

        gymFacade.deleteTrainee("John.Smith", "pass", 1L);

        verify(authenticationService).authenticate("John.Smith", "pass");
        verify(traineeService).delete(1L);
    }

    // deleteTraineeByUsername — auth required

    @Test
    void deleteTraineeByUsernameDelegatesToTraineeServiceAfterAuth() {
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);

        gymFacade.deleteTraineeByUsername("John.Smith", "pass", "John.Smith");

        verify(authenticationService).authenticate("John.Smith", "pass");
        verify(traineeService).delete("John.Smith");
    }

    // findTraineeById — auth required

    @Test
    void findTraineeByIdAuthenticatesAndDelegates() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);
        when(traineeService.find(1L)).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), gymFacade.findTraineeById("John.Smith", "pass", 1L));
        verify(authenticationService).authenticate("John.Smith", "pass");
        verify(traineeService).find(1L);
    }

    // findTraineeByUsername — auth required

    @Test
    void findTraineeByUsernameAuthenticatesAndDelegates() {
        User u = user("John", "Smith");
        Trainee t = trainee(u);
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);
        when(traineeService.find("John.Smith")).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), gymFacade.findTraineeByUsername("John.Smith", "pass", "John.Smith"));
        verify(traineeService).find("John.Smith");
    }

    // authenticateTrainee

    @Test
    void authenticateTraineeDelegatesToAuthService() {
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);

        assertTrue(gymFacade.authenticateTrainee("John.Smith", "pass"));
        verify(authenticationService).authenticate("John.Smith", "pass");
    }

    // changeTraineePassword

    @Test
    void changeTraineePasswordAuthenticatesThenChangesPassword() {
        when(authenticationService.authenticate("John.Smith", "oldPass")).thenReturn(true);

        gymFacade.changeTraineePassword("John.Smith", "oldPass", "newPass");

        verify(authenticationService).authenticate("John.Smith", "oldPass");
        verify(userAccountService).changePassword("John.Smith", "newPass");
    }

    // activateTrainee

    @Test
    void activateTraineeAuthenticatesThenActivates() {
        when(authenticationService.authenticate("admin", "adminPass")).thenReturn(true);

        gymFacade.activateTrainee("admin", "adminPass", "John.Smith");

        verify(authenticationService).authenticate("admin", "adminPass");
        verify(userAccountService).activate("John.Smith");
    }

    // deactivateTrainee

    @Test
    void deactivateTraineeAuthenticatesThenDeactivates() {
        when(authenticationService.authenticate("admin", "adminPass")).thenReturn(true);

        gymFacade.deactivateTrainee("admin", "adminPass", "John.Smith");

        verify(authenticationService).authenticate("admin", "adminPass");
        verify(userAccountService).deactivate("John.Smith");
    }

    // getTraineeTrainings

    @Test
    void getTraineeTrainingsAuthenticatesAndDelegates() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);
        when(trainingService.getTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga"))
                .thenReturn(Set.of());

        Set<Training> result = gymFacade.getTraineeTrainings(
                "John.Smith", "pass", "John.Smith", from, to, "Jane.Doe", "yoga");

        assertTrue(result.isEmpty());
        verify(authenticationService).authenticate("John.Smith", "pass");
        verify(trainingService).getTraineeTrainings("John.Smith", from, to, "Jane.Doe", "yoga");
    }

    // getNotAssignedTrainers

    @Test
    void getNotAssignedTrainersAuthenticatesAndDelegates() {
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);
        when(trainerService.getNotAssignedTrainers("John.Smith")).thenReturn(Set.of());

        gymFacade.getNotAssignedTrainers("John.Smith", "pass", "John.Smith");

        verify(authenticationService).authenticate("John.Smith", "pass");
        verify(trainerService).getNotAssignedTrainers("John.Smith");
    }

    // updateTraineeTrainers

    @Test
    void updateTraineeTrainersAuthenticatesAndDelegates() {
        when(authenticationService.authenticate("John.Smith", "pass")).thenReturn(true);
        Set<String> names = Set.of("Jane.Doe");
        when(traineeService.updateTrainers("John.Smith", names)).thenReturn(Set.of());

        gymFacade.updateTraineeTrainers("John.Smith", "pass", "John.Smith", names);

        verify(authenticationService).authenticate("John.Smith", "pass");
        verify(traineeService).updateTrainers("John.Smith", names);
    }

    // createTrainer — no auth required

    @Test
    void createTrainerDelegatesToTrainerServiceWithoutAuth() {
        User u = user("Jane", "Doe");
        Trainer t = trainer(u);
        when(trainerService.create(t)).thenReturn(t);

        assertSame(t, gymFacade.createTrainer(t));
        verify(trainerService).create(t);
        verifyNoInteractions(authenticationService);
    }

    // updateTrainer — auth required

    @Test
    void updateTrainerAuthenticatesAndDelegates() {
        User u = user("Jane", "Doe");
        Trainer t = trainer(u);
        when(authenticationService.authenticate("Jane.Doe", "pass")).thenReturn(true);
        when(trainerService.update(t)).thenReturn(t);

        assertSame(t, gymFacade.updateTrainer("Jane.Doe", "pass", t));
        verify(authenticationService).authenticate("Jane.Doe", "pass");
        verify(trainerService).update(t);
    }

    @Test
    void updateTrainerThrowsAndSkipsUpdateWhenAuthFails() {
        User u = user("Jane", "Doe");
        Trainer t = trainer(u);
        when(authenticationService.authenticate("Jane.Doe", "bad"))
                .thenThrow(new IllegalArgumentException("bad creds"));

        assertThrows(IllegalArgumentException.class,
                () -> gymFacade.updateTrainer("Jane.Doe", "bad", t));
        verifyNoInteractions(trainerService);
    }

    // findTrainerById — auth required

    @Test
    void findTrainerByIdAuthenticatesAndDelegates() {
        User u = user("Jane", "Doe");
        Trainer t = trainer(u);
        when(authenticationService.authenticate("Jane.Doe", "pass")).thenReturn(true);
        when(trainerService.find(10L)).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), gymFacade.findTrainerById("Jane.Doe", "pass", 10L));
        verify(authenticationService).authenticate("Jane.Doe", "pass");
        verify(trainerService).find(10L);
    }

    // findTrainerByUsername — auth required

    @Test
    void findTrainerByUsernameAuthenticatesAndDelegates() {
        User u = user("Jane", "Doe");
        Trainer t = trainer(u);
        when(authenticationService.authenticate("Jane.Doe", "pass")).thenReturn(true);
        when(trainerService.find("Jane.Doe")).thenReturn(Optional.of(t));

        assertEquals(Optional.of(t), gymFacade.findTrainerByUsername("Jane.Doe", "pass", "Jane.Doe"));
        verify(trainerService).find("Jane.Doe");
    }

    // authenticateTrainer

    @Test
    void authenticateTrainerDelegatesToAuthService() {
        when(authenticationService.authenticate("Jane.Doe", "pass")).thenReturn(true);

        assertTrue(gymFacade.authenticateTrainer("Jane.Doe", "pass"));
        verify(authenticationService).authenticate("Jane.Doe", "pass");
    }

    // changeTrainerPassword

    @Test
    void changeTrainerPasswordAuthenticatesThenChanges() {
        when(authenticationService.authenticate("Jane.Doe", "oldPass")).thenReturn(true);

        gymFacade.changeTrainerPassword("Jane.Doe", "oldPass", "newPass");

        verify(authenticationService).authenticate("Jane.Doe", "oldPass");
        verify(userAccountService).changePassword("Jane.Doe", "newPass");
    }

    // activateTrainer

    @Test
    void activateTrainerAuthenticatesAndActivates() {
        when(authenticationService.authenticate("admin", "adminPass")).thenReturn(true);

        gymFacade.activateTrainer("admin", "adminPass", "Jane.Doe");

        verify(authenticationService).authenticate("admin", "adminPass");
        verify(userAccountService).activate("Jane.Doe");
    }

    // deactivateTrainer

    @Test
    void deactivateTrainerAuthenticatesAndDeactivates() {
        when(authenticationService.authenticate("admin", "adminPass")).thenReturn(true);

        gymFacade.deactivateTrainer("admin", "adminPass", "Jane.Doe");

        verify(authenticationService).authenticate("admin", "adminPass");
        verify(userAccountService).deactivate("Jane.Doe");
    }

    // getTrainerTrainings

    @Test
    void getTrainerTrainingsAuthenticatesAndDelegates() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        when(authenticationService.authenticate("Jane.Doe", "pass")).thenReturn(true);
        when(trainingService.getTrainerTrainings("Jane.Doe", from, to, "John.Smith"))
                .thenReturn(Set.of());

        Set<Training> result = gymFacade.getTrainerTrainings(
                "Jane.Doe", "pass", "Jane.Doe", from, to, "John.Smith");

        assertTrue(result.isEmpty());
        verify(authenticationService).authenticate("Jane.Doe", "pass");
        verify(trainingService).getTrainerTrainings("Jane.Doe", from, to, "John.Smith");
    }

    // addTraining

    @Test
    void addTrainingAuthenticatesAndDelegates() {
        User tu = user("John", "Smith");
        User trainerUser = user("Jane", "Doe");
        Trainee trainee = trainee(tu);
        Trainer trainer = trainer(trainerUser);
        TrainingType type = new TrainingType("yoga");
        LocalDate date = LocalDate.of(2025, 1, 1);
        Training training = new Training(trainee, trainer, "T", type, date, 60);

        when(authenticationService.authenticate("Jane.Doe", "pass")).thenReturn(true);
        when(trainingService.addTraining("John.Smith", "Jane.Doe", "T", "yoga", date, 60))
                .thenReturn(training);

        Training result = gymFacade.addTraining(
                "Jane.Doe", "pass", "John.Smith", "Jane.Doe", "T", "yoga", date, 60);

        assertSame(training, result);
        verify(authenticationService).authenticate("Jane.Doe", "pass");
        verify(trainingService).addTraining("John.Smith", "Jane.Doe", "T", "yoga", date, 60);
    }

    // findTrainingById

    @Test
    void findTrainingByIdAuthenticatesAndDelegates() {
        User tu = user("John", "Smith");
        User trainerUser = user("Jane", "Doe");
        Training training = new Training(trainee(tu), trainer(trainerUser),
                "T", new TrainingType("yoga"), LocalDate.now(), 60);

        when(authenticationService.authenticate("Jane.Doe", "pass")).thenReturn(true);
        when(trainingService.find(100L)).thenReturn(Optional.of(training));

        assertEquals(Optional.of(training), gymFacade.findTrainingById("Jane.Doe", "pass", 100L));
        verify(authenticationService).authenticate("Jane.Doe", "pass");
        verify(trainingService).find(100L);
    }
}
