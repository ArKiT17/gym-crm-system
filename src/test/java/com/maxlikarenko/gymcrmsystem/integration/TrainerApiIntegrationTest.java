package com.maxlikarenko.gymcrmsystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainerApiIntegrationTest extends RestApiIntegrationTestSupport {
    @Test
    void updatesTrainerProfileAndActivationState() throws Exception {
        Credentials trainer = registerTrainer("Profile", "Trainer");

        mockMvc.perform(put("/api/trainers/{username}", trainer.username())
                        .headers(trainer.headers())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Updated","lastName":"Trainer","isActive":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.specialization.id", is(1)));

        mockMvc.perform(patch("/api/user/{username}/status", trainer.username())
                        .headers(trainer.headers())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk());
    }
}
