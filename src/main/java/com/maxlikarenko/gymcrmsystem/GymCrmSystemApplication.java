package com.maxlikarenko.gymcrmsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GymCrmSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCrmSystemApplication.class, args);
    }

}
