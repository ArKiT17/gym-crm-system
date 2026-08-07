package com.maxlikarenko.gymcrmsystem.facade;

import com.maxlikarenko.gymcrmsystem.dto.request.AddTrainingRequest;
import com.maxlikarenko.gymcrmsystem.dto.response.TrainingTypeResponse;
import com.maxlikarenko.gymcrmsystem.mapper.TrainingTypeMapper;
import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.service.TrainingService;
import com.maxlikarenko.gymcrmsystem.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TrainingFacadeTest {
    private TrainingService trainingService;
    private TrainingTypeService trainingTypeService;
    private TrainingTypeMapper trainingTypeMapper;
    private TrainingFacade trainingFacade;

    @BeforeEach
    void setUp() {
        trainingService = mock(TrainingService.class);
        trainingTypeService = mock(TrainingTypeService.class);
        trainingTypeMapper = mock(TrainingTypeMapper.class);
        trainingFacade = new TrainingFacade(trainingService, trainingTypeService, trainingTypeMapper);
    }

    @Test
    void addPassesRequestFieldsToService() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        AddTrainingRequest request =
                new AddTrainingRequest("John.Smith", "Jane.Doe", "Yoga", date, 60);

        trainingFacade.add(request);

        verify(trainingService).addTraining("John.Smith", "Jane.Doe", "Yoga", date, 60);
        verifyNoInteractions(trainingTypeService, trainingTypeMapper);
    }

    @Test
    void getAllTrainingTypesMapsServiceResults() {
        TrainingType yoga = new TrainingType("yoga");
        TrainingTypeResponse response = mock(TrainingTypeResponse.class);
        when(trainingTypeService.getAll()).thenReturn(List.of(yoga));
        when(trainingTypeMapper.toResponse(yoga)).thenReturn(response);

        assertEquals(Set.of(response), trainingFacade.getAllTrainingTypes());

        verify(trainingTypeService).getAll();
        verify(trainingTypeMapper).toResponse(yoga);
        verifyNoInteractions(trainingService);
    }
}
