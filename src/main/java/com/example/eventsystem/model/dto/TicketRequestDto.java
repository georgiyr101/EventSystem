package com.example.eventsystem.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestDto {
    private Long eventId;
    private Long userId;
    private String barcode;
}
