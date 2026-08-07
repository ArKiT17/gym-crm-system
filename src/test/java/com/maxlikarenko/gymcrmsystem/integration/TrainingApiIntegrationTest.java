package com.maxlikarenko.gymcrmsystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingApiIntegrationTest extends RestApiIntegrationTestSupport {
    @Test
    void createsAndQueriesTrainingForTraineeAndTrainer() throws Exception {
        Credentials trainee = registerTrainee("Training", "Trainee");
        Credentials trainer = registerTrainer("Training", "Trainer");

        mockMvc.perform(post("/api/trainings")
                        .headers(trainer.headers())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "traineeUsername": "%s",
                                  "trainerUsername": "%s",
                                  "trainingName": "Yoga session",
                                  "trainingDate": "2026-01-15",
                                  "trainingDuration": 60
                                }
                                """.formatted(trainee.username(), trainer.username())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/trainees/{username}/trainings", trainee.username())
                        .headers(trainee.headers())
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingName", is("Yoga session")))
                .andExpect(jsonPath("$[0].trainingDuration", is(60)));

        mockMvc.perform(get("/api/trainers/{username}/trainings", trainer.username())
                        .headers(trainer.headers()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingName", is("Yoga session")));
    }
}
