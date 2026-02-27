package com.example.eventsystem.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EventResponseDto {
    private Long id;
    private String name;
    private LocalDateTime startDate;
    private String statusDescription;
    private String statusCode;
    private Double ticketPrice;
}