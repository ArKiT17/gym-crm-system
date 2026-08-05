package com.maxlikarenko.gymcrmsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeProfileUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeRegistrationRequest;
import com.maxlikarenko.gymcrmsystem.dto.request.trainee.TraineeTrainersUpdateRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.CredentialsResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainee.TraineeProfileResponse;
import com.maxlikarenko.gymcrmsystem.dto.response.trainer.TrainerSummaryResponse;
import com.maxlikarenko.gymcrmsystem.facade.TraineeFacade;
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

class TraineeControllerTest {
    private TraineeFacade traineeFacade;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        traineeFacade = mock(TraineeFacade.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new TraineeController(traineeFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void createReturnsCreatedCredentials() throws Exception {
        when(traineeFacade.create(any(TraineeRegistrationRequest.class)))
                .thenReturn(new CredentialsResponse("John.Smith", "password"));

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Smith","dateOfBirth":"2000-01-01","address":"Kyiv"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("password"));

        verify(traineeFacade).create(any(TraineeRegistrationRequest.class));
    }

    @Test
    void createRejectsMissingRequiredName() throws Exception {
        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Smith"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeFacade);
    }

    @Test
    void createRejectsFutureDateOfBirth() throws Exception {
        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Smith","dateOfBirth":"2999-01-01"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeFacade);
    }

    @Test
    void getReturnsProfile() throws Exception {
        when(traineeFacade.getByUsername("John.Smith"))
                .thenReturn(mock(TraineeProfileResponse.class));

        mockMvc.perform(get("/api/trainees/John.Smith"))
                .andExpect(status().isOk());

        verify(traineeFacade).getByUsername("John.Smith");
    }

    @Test
    void updateAcceptsOptionalFieldsAndReturnsProfile() throws Exception {
        when(traineeFacade.update(eq("John.Smith"), any(TraineeProfileUpdateRequest.class)))
                .thenReturn(mock(TraineeProfileResponse.class));

        mockMvc.perform(put("/api/trainees/John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeProfileUpdateRequest("John", "Smith", null, null, true))))
                .andExpect(status().isOk());

        verify(traineeFacade).update(eq("John.Smith"), any(TraineeProfileUpdateRequest.class));
    }

    @Test
    void updateRejectsMissingActiveStatus() throws Exception {
        mockMvc.perform(put("/api/trainees/John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Smith"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeFacade);
    }

    @Test
    void deleteReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/trainees/John.Smith"))
                .andExpect(status().isOk());

        verify(traineeFacade).delete("John.Smith");
    }

    @Test
    void availableTrainersReturnsList() throws Exception {
        when(traineeFacade.getNotAssignedTrainers("John.Smith")).thenReturn(Set.of());

        mockMvc.perform(get("/api/trainees/John.Smith/available-trainers"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(traineeFacade).getNotAssignedTrainers("John.Smith");
    }

    @Test
    void updateTrainersAcceptsOneTrainer() throws Exception {
        when(traineeFacade.updateTrainers(eq("John.Smith"), any(TraineeTrainersUpdateRequest.class)))
                .thenReturn(Set.of(mock(TrainerSummaryResponse.class)));

        mockMvc.perform(put("/api/trainees/John.Smith/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trainerUsernames":["Jane.Doe"]}
                                """))
                .andExpect(status().isOk());

        verify(traineeFacade).updateTrainers(eq("John.Smith"), any(TraineeTrainersUpdateRequest.class));
    }

    @Test
    void updateTrainersRejectsEmptyList() throws Exception {
        mockMvc.perform(put("/api/trainees/John.Smith/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trainerUsernames":[]}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeFacade);
    }

    @Test
    void trainingsAcceptsBoundaryDateRangeAndOptionalFilters() throws Exception {
        when(traineeFacade.getTrainings(eq("John.Smith"), any())).thenReturn(Set.of());

        mockMvc.perform(get("/api/trainees/John.Smith/trainings")
                        .param("periodFrom", "2025-01-01")
                        .param("periodTo", "2025-01-01")
                        .param("trainerName", "Jane.Doe")
                        .param("trainingType", "yoga"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(traineeFacade).getTrainings(eq("John.Smith"), any());
    }

    @Test
    void trainingsRejectsReversedDateRange() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Smith/trainings")
                        .param("periodFrom", "2025-12-31")
                        .param("periodTo", "2025-01-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeFacade);
    }
}
