package com.maxlikarenko.gymcrmsystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationApiIntegrationTest extends RestApiIntegrationTestSupport {
    @Test
    void registersAndAuthenticatesTrainee() throws Exception {
        Credentials trainee = registerTrainee("Rest", "Trainee");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", trainee.username())
                        .param("password", trainee.password()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .headers(trainee.headers()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(trainee.username())))
                .andExpect(jsonPath("$.firstName", is("Rest")))
                .andExpect(jsonPath("$.lastName", is("Trainee")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void rejectsProtectedRequestsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/trainees/unknown.user"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "traineeUsername": "unknown.trainee",
                                  "trainerUsername": "unknown.trainer",
                                  "trainingName": "Session",
                                  "trainingDate": "2026-01-01",
                                  "trainingDuration": 60
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesRegistrationAndMalformedDates() throws Exception {
        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"", "lastName":"Trainee"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName", is("First name is required")));

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Rest", "lastName":"Trainee", "dateOfBirth":"not-a-date"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
