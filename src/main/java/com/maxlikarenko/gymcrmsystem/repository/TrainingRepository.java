package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Set;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    @Query("""
        SELECT t FROM Training t
        WHERE t.trainee.user.username = :traineeUsername
            AND (:fromDate IS NULL OR t.date >= :fromDate)
            AND (:toDate IS NULL OR t.date <= :toDate)
            AND (:trainerUsername IS NULL OR t.trainer.user.username = :trainerUsername)
            AND (:trainingTypeName IS NULL OR t.type.name = :trainingTypeName)
    """)
    Set<Training> findTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                       String trainerUsername, String trainingTypeName);

    @Query("""
        SELECT t FROM Training t
        WHERE t.trainer.user.username = :trainerUsername
            AND (:fromDate IS NULL OR t.date >= :fromDate)
            AND (:toDate IS NULL OR t.date <= :toDate)
            AND (:traineeUsername IS NULL OR t.trainee.user.username = :traineeUsername)
    """)
    Set<Training> findTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                        String traineeUsername);
}
