package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ConflictException;
import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.model.dto.AsyncTaskCreatedResponseDto;
import com.example.eventsystem.model.dto.BulkTicketItemRequestDto;
import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.BulkTicketTaskStatusResponseDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.model.enums.BulkTicketTaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAsyncServiceImplTest {

    @Mock
    private TicketBulkAsyncWorker ticketBulkAsyncWorker;

    @InjectMocks
    private TicketAsyncServiceImpl ticketAsyncService;

    @Test
    void submitBulkPurchase_shouldGenerateUniqueTaskIdsFromAtomicCounter() {
        BulkTicketRequestDto request = request();
        CompletableFuture<List<TicketResponseDto>> future = new CompletableFuture<>();
        when(ticketBulkAsyncWorker.buyBulk(request, true)).thenReturn(future);

        AsyncTaskCreatedResponseDto firstTask = ticketAsyncService.submitBulkPurchase(request, true);
        AsyncTaskCreatedResponseDto secondTask = ticketAsyncService.submitBulkPurchase(request, true);

        assertEquals("task-1", firstTask.taskId());
        assertEquals("task-2", secondTask.taskId());
    }

    @Test
    void getTaskStatus_shouldReturnInProgressWhenFutureIsNotDone() {
        BulkTicketRequestDto request = request();
        CompletableFuture<List<TicketResponseDto>> future = new CompletableFuture<>();
        when(ticketBulkAsyncWorker.buyBulk(request, true)).thenReturn(future);

        AsyncTaskCreatedResponseDto task = ticketAsyncService.submitBulkPurchase(request, true);
        BulkTicketTaskStatusResponseDto status = ticketAsyncService.getTaskStatus(task.taskId());

        assertEquals(BulkTicketTaskStatus.IN_PROGRESS, status.status());
        assertNull(status.result());
        assertNull(status.error());
    }

    @Test
    void getTaskStatus_shouldReturnCompletedWithResult() {
        BulkTicketRequestDto request = request();
        TicketResponseDto createdTicket = new TicketResponseDto();
        createdTicket.setId(100L);
        List<TicketResponseDto> completedResult = List.of(createdTicket);
        when(ticketBulkAsyncWorker.buyBulk(request, false)).thenReturn(CompletableFuture.completedFuture(completedResult));

        AsyncTaskCreatedResponseDto task = ticketAsyncService.submitBulkPurchase(request, false);
        BulkTicketTaskStatusResponseDto status = ticketAsyncService.getTaskStatus(task.taskId());

        assertEquals(BulkTicketTaskStatus.COMPLETED, status.status());
        assertEquals(1, status.result().size());
        assertEquals(100L, status.result().getFirst().getId());
        assertNull(status.error());
    }

    @Test
    void getTaskStatus_shouldReturnFailedWhenFutureCompletedExceptionally() {
        BulkTicketRequestDto request = request();
        CompletableFuture<List<TicketResponseDto>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new ConflictException("No more tickets available"));
        when(ticketBulkAsyncWorker.buyBulk(request, true)).thenReturn(failedFuture);

        AsyncTaskCreatedResponseDto task = ticketAsyncService.submitBulkPurchase(request, true);
        BulkTicketTaskStatusResponseDto status = ticketAsyncService.getTaskStatus(task.taskId());

        assertEquals(BulkTicketTaskStatus.FAILED, status.status());
        assertNull(status.result());
        assertEquals("No more tickets available", status.error());
    }

    @Test
    void getTaskStatus_shouldThrowWhenTaskIsMissing() {
        assertThrows(ResourceNotFoundException.class, () -> ticketAsyncService.getTaskStatus("task-999"));
    }

    private BulkTicketRequestDto request() {
        return new BulkTicketRequestDto(1L, List.of(new BulkTicketItemRequestDto(100L, "AAAA1111")));
    }
}
