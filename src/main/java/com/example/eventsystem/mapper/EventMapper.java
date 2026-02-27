package com.example.eventsystem.mapper;

import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.dto.EventResponseDto;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.enums.EventStatus;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public Event toEntity(EventRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        return Event.builder()
                .name(requestDto.getName())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .maxParticipants(requestDto.getMaxParticipants())
                .ticketPrice(requestDto.getTicketPrice())
                .status(EventStatus.PLANNED)
                .build();
    }

    public EventResponseDto toResponseDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .startDate(event.getStartDate())
                .statusCode(event.getStatus().name())
                .statusDescription(event.getStatus().getDescription())
                .ticketPrice(event.getTicketPrice())
                .build();
    }
}