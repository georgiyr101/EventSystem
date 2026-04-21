package com.example.eventsystem.service.impl;

import com.example.eventsystem.model.dto.BulkTicketItemRequestDto;
import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketBulkAsyncWorkerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketBulkAsyncWorker worker;

    @Test
    void buyBulk_shouldUseTransactionalPath() {
        BulkTicketRequestDto request = request();
        List<TicketResponseDto> expected = List.of(new TicketResponseDto());
        when(ticketService.buyTicketsBulkTransactional(request)).thenReturn(expected);

        CompletableFuture<List<TicketResponseDto>> future = worker.buyBulk(request, true);

        assertTrue(future.isDone());
        assertEquals(expected, future.join());
        verify(ticketService).buyTicketsBulkTransactional(request);
    }

    @Test
    void buyBulk_shouldUseNonTransactionalPath() {
        BulkTicketRequestDto request = request();
        List<TicketResponseDto> expected = List.of(new TicketResponseDto(), new TicketResponseDto());
        when(ticketService.buyTicketsBulkNonTransactional(request)).thenReturn(expected);

        CompletableFuture<List<TicketResponseDto>> future = worker.buyBulk(request, false);

        assertTrue(future.isDone());
        assertEquals(expected, future.join());
        verify(ticketService).buyTicketsBulkNonTransactional(request);
    }

    private BulkTicketRequestDto request() {
        return new BulkTicketRequestDto(1L, List.of(new BulkTicketItemRequestDto(100L, "AAAA1111")));
    }
}
