package com.example.eventsystem.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {
    private Long id;
    private Long eventId;
    private String eventName;
    private Long userId;
    private String userEmail;
    private String barcode;
    private LocalDateTime purchaseDate;
}
