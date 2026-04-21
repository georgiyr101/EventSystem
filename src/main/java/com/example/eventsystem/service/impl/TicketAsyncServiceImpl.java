package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.model.dto.AsyncTaskCreatedResponseDto;
import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.BulkTicketTaskStatusResponseDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.model.enums.BulkTicketTaskStatus;
import com.example.eventsystem.service.TicketAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class TicketAsyncServiceImpl implements TicketAsyncService {

    private static final String TASK_ID_PREFIX = "task-";

    private final TicketBulkAsyncWorker ticketBulkAsyncWorker;
    private final AtomicLong taskCounter = new AtomicLong(1L);
    private final Map<String, CompletableFuture<List<TicketResponseDto>>> tasks = new ConcurrentHashMap<>();

    @Override
    public AsyncTaskCreatedResponseDto submitBulkPurchase(BulkTicketRequestDto request, boolean transactional) {
        String taskId = TASK_ID_PREFIX + taskCounter.getAndIncrement();
        CompletableFuture<List<TicketResponseDto>> taskFuture = ticketBulkAsyncWorker.buyBulk(request, transactional);
        tasks.put(taskId, taskFuture);
        return new AsyncTaskCreatedResponseDto(taskId);
    }

    @Override
    public BulkTicketTaskStatusResponseDto getTaskStatus(String taskId) {
        CompletableFuture<List<TicketResponseDto>> taskFuture = tasks.get(taskId);
        if (taskFuture == null) {
            throw new ResourceNotFoundException("Async task not found with id: " + taskId);
        }

        if (!taskFuture.isDone()) {
            return new BulkTicketTaskStatusResponseDto(taskId, BulkTicketTaskStatus.IN_PROGRESS, null, null);
        }

        if (taskFuture.isCompletedExceptionally()) {
            return new BulkTicketTaskStatusResponseDto(
                    taskId,
                    BulkTicketTaskStatus.FAILED,
                    null,
                    extractErrorMessage(taskFuture));
        }

        return new BulkTicketTaskStatusResponseDto(
                taskId,
                BulkTicketTaskStatus.COMPLETED,
                taskFuture.getNow(List.of()),
                null);
    }

    private String extractErrorMessage(CompletableFuture<List<TicketResponseDto>> taskFuture) {
        Throwable throwable = taskFuture.handle((result, error) -> error).join();
        Throwable cause = unwrapCompletionCause(throwable);
        if (cause == null || cause.getMessage() == null || cause.getMessage().isBlank()) {
            return "Async task failed";
        }
        return cause.getMessage();
    }

    private Throwable unwrapCompletionCause(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
