package com.maxlikarenko.gymcrmsystem.metrics;

import com.maxlikarenko.gymcrmsystem.repository.TraineeRepository;
import com.maxlikarenko.gymcrmsystem.repository.TrainerRepository;
import com.maxlikarenko.gymcrmsystem.repository.TrainingRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics implements MeterBinder {
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;

    public GymMetrics(TrainerRepository trainerRepository,
                      TraineeRepository traineeRepository,
                      TrainingRepository trainingRepository) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingRepository = trainingRepository;
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        Gauge.builder("gym.trainers.active", trainerRepository, TrainerRepository::countByUserActiveTrue)
                .description("Number of active trainers")
                .register(registry);
        Gauge.builder("gym.trainees.active", traineeRepository, TraineeRepository::countByUserActiveTrue)
                .description("Number of active trainees")
                .register(registry);
        Gauge.builder("gym.trainings.created", trainingRepository, TrainingRepository::count)
                .description("Current number of created trainings")
                .register(registry);
    }
}
