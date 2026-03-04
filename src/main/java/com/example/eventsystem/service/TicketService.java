package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import java.util.List;

public interface TicketService {

    TicketResponseDto buyTicket(TicketRequestDto dto);

    List<TicketResponseDto> getTicketsByUser(Long userId);
}