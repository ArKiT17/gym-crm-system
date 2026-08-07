package com.maxlikarenko.gymcrmsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maxlikarenko.gymcrmsystem.dto.request.AddTrainingRequest;
import com.maxlikarenko.gymcrmsystem.facade.TrainingFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingControllerTest {
    private TrainingFacade trainingFacade;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        trainingFacade = mock(TrainingFacade.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new TrainingController(trainingFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void addAcceptsPositiveDuration() throws Exception {
        AddTrainingRequest request =
                new AddTrainingRequest("John.Smith", "Jane.Doe", "Yoga", LocalDate.of(2025, 1, 1), 1);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainingFacade).add(any(AddTrainingRequest.class));
    }

    @Test
    void addRejectsZeroDurationAndMissingName() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"traineeUsername":"John.Smith","trainerUsername":"Jane.Doe","trainingName":"","trainingDate":"2025-01-01","trainingDuration":0}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingFacade);
    }

    @Test
    void addRejectsMissingTrainingDate() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"traineeUsername":"John.Smith","trainerUsername":"Jane.Doe","trainingName":"Yoga","trainingDuration":60}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingFacade);
    }
}
