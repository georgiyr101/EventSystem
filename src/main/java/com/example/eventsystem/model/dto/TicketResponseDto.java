package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные билета")
public class TicketResponseDto {

    @Schema(description = "Идентификатор билета", example = "101")
    private Long id;

    @Schema(description = "Идентификатор мероприятия", example = "15")
    private Long eventId;

    @Schema(description = "Название мероприятия", example = "Spring Boot Meetup")
    private String eventName;

    @Schema(description = "Идентификатор пользователя", example = "7")
    private Long userId;

    @Schema(description = "Email пользователя", example = "user@example.com")
    private String userEmail;

    @Schema(description = "Штрих-код билета", example = "ABC12345-01")
    private String barcode;

    @Schema(description = "Дата и время покупки", example = "2026-04-01T12:30:00")
    private LocalDateTime purchaseDate;
}
