package com.example.eventsystem.mapper;

import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.model.entity.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponseDto toResponseDto(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(ticket.getId());
        dto.setBarcode(ticket.getBarcode());
        dto.setPurchaseDate(ticket.getPurchaseDate());

        if (ticket.getEvent() != null) {
            dto.setEventId(ticket.getEvent().getId());
            dto.setEventName(ticket.getEvent().getName());
        }

        if (ticket.getUser() != null) {
            dto.setUserId(ticket.getUser().getId());
            dto.setUserEmail(ticket.getUser().getEmail());
        }

        return dto;
    }
}