package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.RaceConditionDemoResponse;

public interface ConcurrencyDemoService {

    RaceConditionDemoResponse runRaceConditionDemo(int threads, int incrementsPerThread);
}
