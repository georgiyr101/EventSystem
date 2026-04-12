package com.example.eventsystem.service.impl;

import com.example.eventsystem.model.dto.RaceConditionDemoResponse;
import com.example.eventsystem.service.ConcurrencyDemoService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ConcurrencyDemoServiceImpl implements ConcurrencyDemoService {

    private static final int MIN_THREADS_FOR_DEMO = 50;

    @Override
    public RaceConditionDemoResponse runRaceConditionDemo(final int threads, final int incrementsPerThread) {
        int actualThreads = Math.max(threads, MIN_THREADS_FOR_DEMO);
        int expected = actualThreads * incrementsPerThread;

        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        AtomicInteger atomicCounter = new AtomicInteger();

        ExecutorService executorService = createExecutorService(actualThreads);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < actualThreads; i++) {
            tasks.add(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    unsafeCounter.increment();
                    synchronizedCounter.increment();
                    atomicCounter.incrementAndGet();
                }
                return null;
            });
        }

        try {
            waitForTasks(executorService, tasks);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread was interrupted during race condition demo", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Error while executing race condition demo", exception);
        } finally {
            shutdownExecutor(executorService);
        }

        log.info("Race demo finished. expected={}, unsafe={}, synchronized={}, atomic={}",
                expected, unsafeCounter.getValue(), synchronizedCounter.getValue(), atomicCounter.get());

        return RaceConditionDemoResponse.builder()
                .threads(actualThreads)
                .incrementsPerThread(incrementsPerThread)
                .expected(expected)
                .unsafeResult(unsafeCounter.getValue())
                .synchronizedResult(synchronizedCounter.getValue())
                .atomicResult(atomicCounter.get())
                .build();
    }

    ExecutorService createExecutorService(final int actualThreads) {
        return Executors.newFixedThreadPool(actualThreads);
    }

    void waitForTasks(final ExecutorService executorService, final List<Callable<Void>> tasks)
            throws InterruptedException, ExecutionException {
        List<Future<Void>> futures = executorService.invokeAll(tasks);
        for (Future<Void> future : futures) {
            future.get();
        }
    }

    private void shutdownExecutor(final ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Getter
    private static class UnsafeCounter {
        private int value;

        public void increment() {
            value++;
        }
    }

    private static final class SynchronizedCounter {
        private int value;

        public synchronized void increment() {
            value++;
        }

        public synchronized int getValue() {
            return value;
        }
    }
}
