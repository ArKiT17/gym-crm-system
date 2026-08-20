package com.maxlikarenko.gymcrmsystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationApiIntegrationTest extends RestApiIntegrationTestSupport {
    @Test
    void registersAndAuthenticatesTrainee() throws Exception {
        Credentials trainee = registerTrainee("Rest", "Trainee");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", trainee.username())
                        .param("password", trainee.password()))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Transaction-Id"));

        mockMvc.perform(get("/api/trainees/{username}", trainee.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(trainee.username())))
                .andExpect(jsonPath("$.firstName", is("Rest")))
                .andExpect(jsonPath("$.lastName", is("Trainee")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void generatesAndPropagatesTransactionId() throws Exception {
        String firstTransactionId = mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Transaction-Id");

        String propagatedTransactionId = UUID.randomUUID().toString();
        String secondTransactionId = mockMvc.perform(get("/api/training-types")
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
