package com.maxlikarenko.gymcrmsystem.storage;

import com.maxlikarenko.gymcrmsystem.model.Trainee;
import com.maxlikarenko.gymcrmsystem.model.Trainer;
import com.maxlikarenko.gymcrmsystem.model.Training;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class StorageData {
    private Map<Long, Trainee> trainees = new HashMap<>();
    private Map<Long, Trainer> trainers = new HashMap<>();
    private Map<Long, Training> trainings = new HashMap<>();
}
