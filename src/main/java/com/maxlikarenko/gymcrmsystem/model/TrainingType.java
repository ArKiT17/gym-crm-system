package com.maxlikarenko.gymcrmsystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "training_types")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String name;

    public TrainingType(String name) {
        this.name = name;
    }
}
