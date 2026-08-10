package com.maxlikarenko.gymcrmsystem.repository;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findByUserUsername(String username);

    Integer countByUserActiveTrue();
}
