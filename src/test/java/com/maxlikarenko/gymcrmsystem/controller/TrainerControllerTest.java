package com.maxlikarenko.gymcrmsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerProfileUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainer.TrainerRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.CredentialsResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerProfileResponse;
import com.maxlikarenko.gymcrmsystem.facade.TrainerFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrainerControllerTest {
    private TrainerFacade trainerFacade;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        trainerFacade = mock(TrainerFacade.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new TrainerController(trainerFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createReturnsCreatedCredentials() throws Exception {
        when(trainerFacade.create(any(TrainerRegistrationRequest.class)))
                .thenReturn(new CredentialsResponse("Jane.Doe", "password"));

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Jane","lastName":"Doe","specializationId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Jane.Doe"));

        verify(trainerFacade).create(any(TrainerRegistrationRequest.class));
    }

    @Test
    void createRejectsNonPositiveSpecialization() throws Exception {
        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Jane","lastName":"Doe","specializationId":0}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerFacade);
    }

    @Test
    void getReturnsProfile() throws Exception {
        when(trainerFacade.getByUsername("Jane.Doe"))
                .thenReturn(mock(TrainerProfileResponse.class));

        mockMvc.perform(get("/api/trainers/Jane.Doe"))
                .andExpect(status().isOk());

        verify(trainerFacade).getByUsername("Jane.Doe");
    }

    @Test
    void updateAcceptsBothActiveBoundaryValues() throws Exception {
        when(trainerFacade.update(eq("Jane.Doe"), any(TrainerProfileUpdateRequest.class)))
                .thenReturn(mock(TrainerProfileResponse.class));

        mockMvc.perform(put("/api/trainers/Jane.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerProfileUpdateRequest("Jane", "Doe", true))))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/trainers/Jane.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerProfileUpdateRequest("Jane", "Doe", false))))
                .andExpect(status().isOk());

        verify(trainerFacade, times(2)).update(eq("Jane.Doe"), any(TrainerProfileUpdateRequest.class));
    }

    @Test
    void updateRejectsMissingLastName() throws Exception {
        mockMvc.perform(put("/api/trainers/Jane.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Jane","lastName":"","isActive":true}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerFacade);
    }

    @Test
    void trainingsAcceptsOptionalFilters() throws Exception {
        when(trainerFacade.getTrainings(eq("Jane.Doe"), any())).thenReturn(Set.of());

        mockMvc.perform(get("/api/trainers/Jane.Doe/trainings")
                        .param("periodFrom", "2025-01-01")
                        .param("periodTo", "2025-12-31")
                        .param("traineeName", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(trainerFacade).getTrainings(eq("Jane.Doe"), any());
    }

    @Test
    void trainingsRejectsReversedDateRange() throws Exception {
        mockMvc.perform(get("/api/trainers/Jane.Doe/trainings")
                        .param("periodFrom", "2025-12-31")
                        .param("periodTo", "2025-01-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerFacade);
    }
}
