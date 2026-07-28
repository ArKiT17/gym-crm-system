package com.maxlikarenko.gymcrmsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "training_name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType type;

    @Column(name = "training_date", nullable = false)
    private LocalDate date;

    @Column(name = "training_duration", nullable = false)
    private int duration;

    public Training(Trainee trainee, Trainer trainer, String name, TrainingType type, LocalDate date, int duration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.name = name;
        this.type = type;
        this.date = date;
        this.duration = duration;
    }
}
