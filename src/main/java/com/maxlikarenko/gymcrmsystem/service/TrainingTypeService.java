package com.maxlikarenko.gymcrmsystem.service;

import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingTypeRepository;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TrainingTypeService {
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    public TrainingType get(Long id) {
        log.debug("Finding training type with id {}", id);
        return trainingTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training type with id " + id + " not found"));
    }

    public TrainingType get(String name) {
        log.debug("Finding training type with name {}", name);
        return trainingTypeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Training type with name " + name + " not found"));
    }

    public List<TrainingType> getAll() {
        log.debug("Finding all training types");
        return trainingTypeRepository.findAll();
    }
}
