package com.maxlikarenko.gymcrmsystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationApiIntegrationTest extends RestApiIntegrationTestSupport {
    @Test
    void registersAndAuthenticatesTrainee() throws Exception {
        Credentials trainee = registerTrainee("Rest", "Trainee");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(trainee.username(), trainee.password())))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Transaction-Id"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(jwtExpirationSeconds)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(trainee.username())))
                .andExpect(jsonPath("$.firstName", is("Rest")))
                .andExpect(jsonPath("$.lastName", is("Trainee")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void rejectsProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void locksLoginAfterThreeFailedAttempts() throws Exception {
        Credentials trainee = registerTrainee("Brute", "Force");
        String invalidLogin = """
                {"username":"%s","password":"wrong-password"}
                """.formatted(trainee.username());

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidLogin))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail", is("Too many login attempts. Try again later.")));
    }

    @Test
    void logoutRevokesAccessToken() throws Exception {
        Credentials trainee = registerTrainee("Logout", "Trainee");

        mockMvc.perform(post("/api/auth/logout")
                        .headers(trainee.headers()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/trainees/{username}", trainee.username())
                        .headers(trainee.headers()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAccessToAnotherUsersProfile() throws Exception {
        Credentials trainee = registerTrainee("Owner", "Trainee");
        Credentials trainer = registerTrainer("Other", "Trainer");

        mockMvc.perform(get("/api/trainers/{username}", trainer.username())
                        .headers(trainee.headers()))
                .andExpect(status().isForbidden());
    }

    @Test
    void generatesAndPropagatesTransactionId() throws Exception {
        Credentials trainee = registerTrainee("Transaction", "Trainee");

        String firstTransactionId = mockMvc.perform(get("/api/training-types")
                        .headers(trainee.headers()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Transaction-Id");

        String propagatedTransactionId = UUID.randomUUID().toString();
        String secondTransactionId = mockMvc.perform(get("/api/training-types")
                        .headers(trainee.headers())
                        .header("X-Transaction-Id", propagatedTransactionId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Transaction-Id");

        assertTrue(firstTransactionId != null && !firstTransactionId.isBlank());
        assertEquals(propagatedTransactionId, secondTransactionId);
        assertNotEquals(firstTransactionId, secondTransactionId);
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
