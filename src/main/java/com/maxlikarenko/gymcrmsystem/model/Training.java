package com.maxlikarenko.gymcrmsystem.model;

import lombok.*;

import java.time.Duration;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Training {
    private Long id;
    private Trainer trainer;
    private Trainee trainee;
    private String name;
    private TrainingType type;
    private LocalDate date;
    private Duration duration;
}
