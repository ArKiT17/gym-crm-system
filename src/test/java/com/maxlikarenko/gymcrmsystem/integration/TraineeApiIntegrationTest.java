package com.maxlikarenko.gymcrmsystem.integration;

import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import com.maxlikarenko.gymcrmsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TraineeApiIntegrationTest extends RestApiIntegrationTestSupport {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TraineeRepository traineeRepository;
    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    void updatesTraineeProfileAndTrainerList() throws Exception {
        Credentials trainee = registerTrainee("Update", "Trainee");
        Credentials trainer = registerTrainer("Update", "Trainer");

        mockMvc.perform(put("/api/trainees/{username}", trainee.username())
                        .headers(trainee.headers())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Updated",
                                  "lastName": "Trainee",
                                  "dateOfBirth": "2000-01-01",
                                  "address": "Updated address",
                                  "isActive": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.address", is("Updated address")));

        mockMvc.perform(put("/api/trainees/{username}/trainers", trainee.username())
                        .headers(trainee.headers())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainerUsernames\":[\"" + trainer.username() + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is(trainer.username())));

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .headers(trainee.headers()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers", hasSize(1)));
    }

    @Test
    void deletesTraineeAndCascadesTraining() throws Exception {
        Credentials trainee = registerTrainee("Delete", "Trainee");
        Credentials trainer = registerTrainer("Delete", "Trainer");

        mockMvc.perform(postTraining(trainee, trainer))
                .andExpect(status().isOk());
        flushAndClear();
        long trainingCount = trainingRepository.count();
        long userCount = userRepository.count();

        mockMvc.perform(delete("/api/trainees/{username}", trainee.username())
                        .headers(trainee.headers()))
                .andExpect(status().isOk());
        flushAndClear();

        assertEquals(trainingCount - 1, trainingRepository.count());
        assertEquals(userCount - 1, userRepository.count());
        assertTrue(traineeRepository.findByUserUsername(trainee.username()).isEmpty());
    }

    @Test
    void returnsProblemDetailsForUnknownResourceAndUnsupportedMethod() throws Exception {
        Credentials trainee = registerTrainee("Errors", "Trainee");

        mockMvc.perform(get("/api/trainees/does.not.exist")
                        .headers(trainee.headers()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Trainee with username does.not.exist not found")));

        mockMvc.perform(post("/api/trainees/" + trainee.username())
                        .headers(trainee.headers()))
                .andExpect(status().isMethodNotAllowed());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postTraining(
            Credentials trainee, Credentials trainer) {
        return post("/api/trainings")
                .headers(trainer.headers())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "traineeUsername": "%s",
                          "trainerUsername": "%s",
                          "trainingName": "Delete session",
                          "trainingDate": "2026-02-01",
                          "trainingDuration": 45
                        }
                        """.formatted(trainee.username(), trainer.username()));
    }
}
