package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import java.util.List;

public interface TicketService {

    TicketResponseDto buyTicket(TicketRequestDto dto);

    TicketResponseDto getById(Long id);

    List<TicketResponseDto> getTickets(Long userId, String barcode);

    TicketResponseDto update(Long id, TicketRequestDto dto);

    void delete(Long id);
}