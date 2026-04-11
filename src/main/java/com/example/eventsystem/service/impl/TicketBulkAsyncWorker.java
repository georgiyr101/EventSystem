package com.example.eventsystem.service.impl;

import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TicketBulkAsyncWorker {

    private final TicketService ticketService;

    @Async
    public CompletableFuture<List<TicketResponseDto>> buyBulk(BulkTicketRequestDto request, boolean transactional) {
        List<TicketResponseDto> result = transactional
                ? ticketService.buyTicketsBulkTransactional(request)
                : ticketService.buyTicketsBulkNonTransactional(request);
        return CompletableFuture.completedFuture(result);
    }
}
