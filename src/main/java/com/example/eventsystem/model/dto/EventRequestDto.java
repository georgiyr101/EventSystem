package com.example.eventsystem.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventRequestDto {
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxParticipants;
    private Double ticketPrice;
}