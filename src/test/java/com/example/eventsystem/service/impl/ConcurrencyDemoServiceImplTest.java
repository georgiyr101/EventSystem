package com.example.eventsystem.service.impl;

import com.example.eventsystem.model.dto.RaceConditionDemoResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void runRaceConditionDemo_shouldWrapInterruptedException() throws Exception {
        ConcurrencyDemoServiceImpl serviceSpy = spy(new ConcurrencyDemoServiceImpl());
        ExecutorService executorService = mock(ExecutorService.class);
        doReturn(executorService).when(serviceSpy).createExecutorService(anyInt());
        doThrow(new InterruptedException("interrupted")).when(serviceSpy).waitForTasks(any(), any());
        when(executorService.awaitTermination(anyLong(), any())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> serviceSpy.runRaceConditionDemo(60, 10));

        assertTrue(ex.getMessage().contains("interrupted"));
        verify(executorService).shutdown();
        verify(executorService).awaitTermination(anyLong(), any());
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void runRaceConditionDemo_shouldWrapExecutionException() throws Exception {
        ConcurrencyDemoServiceImpl serviceSpy = spy(new ConcurrencyDemoServiceImpl());
        ExecutorService executorService = mock(ExecutorService.class);
        doReturn(executorService).when(serviceSpy).createExecutorService(anyInt());
        doThrow(new ExecutionException(new RuntimeException("boom"))).when(serviceSpy).waitForTasks(any(), any());
        when(executorService.awaitTermination(anyLong(), any())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> serviceSpy.runRaceConditionDemo(60, 10));

        assertTrue(ex.getMessage().contains("executing race condition demo"));
        verify(executorService).shutdown();
        verify(executorService).awaitTermination(anyLong(), any());
    }

    @Test
    void runRaceConditionDemo_shouldCallShutdownNowWhenExecutorDoesNotTerminate() throws Exception {
        ConcurrencyDemoServiceImpl serviceSpy = spy(new ConcurrencyDemoServiceImpl());
        ExecutorService executorService = mock(ExecutorService.class);
        doReturn(executorService).when(serviceSpy).createExecutorService(anyInt());
        doNothing().when(serviceSpy).waitForTasks(any(), any());
        when(executorService.awaitTermination(anyLong(), any())).thenReturn(false);

        serviceSpy.runRaceConditionDemo(60, 10);

        verify(executorService).shutdown();
        verify(executorService).shutdownNow();
    }

    @Test
    void runRaceConditionDemo_shouldHandleInterruptedDuringShutdown() throws Exception {
        ConcurrencyDemoServiceImpl serviceSpy = spy(new ConcurrencyDemoServiceImpl());
        ExecutorService executorService = mock(ExecutorService.class);
        doReturn(executorService).when(serviceSpy).createExecutorService(anyInt());
        doNothing().when(serviceSpy).waitForTasks(any(), any());
        when(executorService.awaitTermination(anyLong(), any())).thenThrow(new InterruptedException("shutdown"));

        serviceSpy.runRaceConditionDemo(60, 10);

        verify(executorService).shutdown();
        verify(executorService).shutdownNow();
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }
}
