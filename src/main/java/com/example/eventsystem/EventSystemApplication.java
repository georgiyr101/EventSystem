package com.example.eventsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventSystemApplication.class, args);
    }
}
