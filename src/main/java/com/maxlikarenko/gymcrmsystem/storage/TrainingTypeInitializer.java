package com.maxlikarenko.gymcrmsystem.storage;

import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TrainingTypeInitializer {

    public static final List<String> REQUIRED_TRAINING_TYPES = List.of(
            "fitness", "yoga", "zumba", "stretching", "resistance"
    );

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeInitializer(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @PostConstruct
    public void init() {
        for (String name : REQUIRED_TRAINING_TYPES) {
            if (trainingTypeRepository.findByName(name).isEmpty()) {
                trainingTypeRepository.save(new TrainingType(name));
                log.info("Seeded training type: {}", name);
            }
        }
        log.info("TrainingType seed complete ({} types)", trainingTypeRepository.count());
    }
}
