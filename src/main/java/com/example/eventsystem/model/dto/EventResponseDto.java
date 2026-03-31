package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Данные мероприятия")
public class EventResponseDto {

    @Schema(description = "Идентификатор мероприятия", example = "15")
    private Long id;

    @Schema(description = "Название мероприятия", example = "Spring Boot Meetup")
    private String name;

    @Schema(description = "Дата и время начала", example = "2026-06-15T18:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Описание статуса", example = "Опубликовано")
    private String statusDescription;

    @Schema(description = "Код статуса", example = "PUBLISHED")
    private String statusCode;

    @Schema(description = "Цена билета", example = "49.99")
    private Double ticketPrice;

    @Schema(description = "Имя организатора", example = "Tech Community")
    private String organizerName;

    @ArraySchema(schema = @Schema(description = "Название категории", example = "Technology"),
            arraySchema = @Schema(description = "Список категорий мероприятия"))
    private List<String> categoryNames;
}
