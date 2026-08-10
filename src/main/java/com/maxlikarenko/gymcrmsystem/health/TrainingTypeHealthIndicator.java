package com.maxlikarenko.gymcrmsystem.health;

import com.maxlikarenko.gymcrmsystem.model.TrainingType;
import com.maxlikarenko.gymcrmsystem.repository.TrainingTypeRepository;
import com.maxlikarenko.gymcrmsystem.storage.TrainingTypeInitializer;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("trainingTypes")
public class TrainingTypeHealthIndicator implements HealthIndicator {
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        List<String> loadedTypes = trainingTypeRepository.findAll().stream()
                .map(TrainingType::getName)
                .toList();
        List<String> missingTypes = TrainingTypeInitializer.REQUIRED_TRAINING_TYPES.stream()
                .filter(name -> !loadedTypes.contains(name))
                .toList();

        if (missingTypes.isEmpty()) {
            return Health.up()
                    .withDetail("seeded", true)
                    .withDetail("count", TrainingTypeInitializer.REQUIRED_TRAINING_TYPES.size())
                    .withDetail("types", TrainingTypeInitializer.REQUIRED_TRAINING_TYPES)
                    .build();
        }

        return Health.down()
                    .withDetail("seeded", false)
                    .withDetail("missing", missingTypes)
                    .withDetail("count", TrainingTypeInitializer.REQUIRED_TRAINING_TYPES.size() - missingTypes.size())
                    .build();
    }
}
