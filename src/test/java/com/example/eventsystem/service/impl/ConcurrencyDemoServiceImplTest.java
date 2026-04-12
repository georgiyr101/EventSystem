package com.example.eventsystem.service.impl;

import com.example.eventsystem.model.dto.RaceConditionDemoResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyDemoServiceImplTest {

    private final ConcurrencyDemoServiceImpl service = new ConcurrencyDemoServiceImpl();

    @Test
    void runRaceConditionDemo_shouldUseAtLeastFiftyThreads() {
        RaceConditionDemoResponse response = service.runRaceConditionDemo(10, 1000);

        assertEquals(50, response.getThreads());
        assertEquals(50_000, response.getExpected());
    }

    @Test
    void runRaceConditionDemo_shouldKeepSynchronizedAndAtomicAccurate() {
        RaceConditionDemoResponse response = service.runRaceConditionDemo(60, 2000);

        assertEquals(response.getExpected(), response.getSynchronizedResult());
        assertEquals(response.getExpected(), response.getAtomicResult());
        assertTrue(response.getUnsafeResult() <= response.getExpected());
    }
}
