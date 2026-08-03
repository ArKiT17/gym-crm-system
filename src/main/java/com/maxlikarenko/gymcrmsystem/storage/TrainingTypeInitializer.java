package com.maxlikarenko.gymcrmsystem.storage;

import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TrainingTypeInitializer {

    private static final List<String> TRAINING_TYPES = List.of(
            "fitness", "yoga", "zumba", "stretching", "resistance"
    );

    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @PostConstruct
    public void init() {
        for (String name : TRAINING_TYPES) {
            if (trainingTypeRepository.findByName(name).isEmpty()) {
                trainingTypeRepository.save(new TrainingType(name));
                log.info("Seeded training type: {}", name);
            }
        }
        log.info("TrainingType seed complete ({} types)", trainingTypeRepository.count());
    }
}
