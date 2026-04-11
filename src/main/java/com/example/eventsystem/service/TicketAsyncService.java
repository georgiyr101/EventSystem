package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.AsyncTaskCreatedResponseDto;
import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.BulkTicketTaskStatusResponseDto;

public interface TicketAsyncService {

    AsyncTaskCreatedResponseDto submitBulkPurchase(BulkTicketRequestDto request, boolean transactional);

    BulkTicketTaskStatusResponseDto getTaskStatus(String taskId);
}
