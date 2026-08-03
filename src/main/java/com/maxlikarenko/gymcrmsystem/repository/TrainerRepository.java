package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUserUsername(String username);

    Set<Trainer> findByUserUsernameIn(Set<String> usernames);

    @Query("""
        SELECT tr FROM Trainer tr
        WHERE tr.id NOT IN (
                SELECT tr2.id FROM Trainee t
                JOIN t.trainers tr2
                WHERE t.user.username = :traineeUsername
        )
    """)
    Set<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername);
}
