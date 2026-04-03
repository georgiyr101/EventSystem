package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import java.util.List;

public interface TicketService {

    TicketResponseDto buyTicket(TicketRequestDto dto);

    List<TicketResponseDto> buyTicketsBulkTransactional(BulkTicketRequestDto request);

    List<TicketResponseDto> buyTicketsBulkNonTransactional(BulkTicketRequestDto request);

    TicketResponseDto getById(Long id);

    List<TicketResponseDto> getTickets(Long userId, String barcode);

    TicketResponseDto update(Long id, TicketRequestDto dto);

    void delete(Long id);

    List<TicketResponseDto> getAllTickets();
}
