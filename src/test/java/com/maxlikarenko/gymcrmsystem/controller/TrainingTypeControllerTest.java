package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.facade.TrainingFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrainingTypeControllerTest {
    private TrainingFacade trainingFacade;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        trainingFacade = mock(TrainingFacade.class);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new TrainingTypeController(trainingFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllReturnsTrainingTypes() throws Exception {
        when(trainingFacade.getAllTrainingTypes())
                .thenReturn(Set.of(new TrainingTypeResponse(1L, "yoga")));

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("yoga"));

        verify(trainingFacade).getAllTrainingTypes();
    }

    @Test
    void getAllReturnsEmptyCollection() throws Exception {
        when(trainingFacade.getAllTrainingTypes()).thenReturn(Set.of());

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(trainingFacade).getAllTrainingTypes();
    }
}
