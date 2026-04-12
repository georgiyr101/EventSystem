package com.example.eventsystem.service.impl;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceConditionDemoTest {

    private static final int THREADS = 64;
    private static final int INCREMENTS_PER_THREAD = 20_000;

    @Test
    void shouldDemonstrateRaceConditionWithUnsafeCounter() throws InterruptedException {
        int expected = THREADS * INCREMENTS_PER_THREAD;
        boolean raceDetected = false;
        int lastActual = expected;

        for (int attempt = 0; attempt < 10 && !raceDetected; attempt++) {
            UnsafeCounter counter = new UnsafeCounter();
            int actual = runConcurrentIncrement(counter);
            lastActual = actual;
            raceDetected = actual < expected;
        }

        System.out.println("UnsafeCounter expected=" + expected + ", actual=" + lastActual);
        assertTrue(raceDetected,
                "Race condition was not detected. Increase threads or increments to reproduce.");
    }

    @Test
    void shouldSolveRaceConditionWithAtomicCounter() throws InterruptedException {
        int expected = THREADS * INCREMENTS_PER_THREAD;
        SafeAtomicCounter counter = new SafeAtomicCounter();

        int actual = runConcurrentIncrement(counter);

        System.out.println("SafeAtomicCounter expected=" + expected + ", actual=" + actual);
        assertEquals(expected, actual);
    }

    private int runConcurrentIncrement(Counter counter) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        CountDownLatch readyLatch = new CountDownLatch(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                        counter.increment();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        finishLatch.await(20, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        return counter.get();
    }

    private interface Counter {
        void increment();

        int get();
    }

    private static final class UnsafeCounter implements Counter {
        private int value;

        @Override
        public void increment() {
            value++;
        }

        @Override
        public int get() {
            return value;
        }
    }

    private static final class SafeAtomicCounter implements Counter {
        private final AtomicInteger value = new AtomicInteger();

        @Override
        public void increment() {
            value.incrementAndGet();
        }

        @Override
        public int get() {
            return value.get();
        }
    }
}
