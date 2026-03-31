package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Данные для создания или обновления мероприятия")
public class EventRequestDto {

    @Schema(description = "Название мероприятия", example = "Spring Boot Meetup")
    @NotBlank(message = "Название мероприятия обязательно")
    private String name;

    @Schema(description = "Дата и время начала", example = "2026-06-15T18:00:00")
    @NotNull(message = "Дата начала обязательна")
    @Future(message = "Мероприятие должно быть в будущем")
    private LocalDateTime startDate;

    @Schema(description = "Дата и время окончания", example = "2026-06-15T22:00:00")
    @NotNull(message = "Дата окончания обязательна")
    private LocalDateTime endDate;

    @Schema(description = "Максимальное число участников", example = "250")
    @NotNull(message = "Максимальное количество участников обязательно")
    @Min(value = 1, message = "Количество участников должно быть не менее 1")
    private Integer maxParticipants;

    @Schema(description = "Цена билета", example = "49.99")
    @NotNull(message = "Цена билета обязательна")
    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private Double ticketPrice;

    @Schema(description = "Идентификатор организатора", example = "10")
    @NotNull(message = "ID организатора обязателен")
    private Long organizerId;

    @ArraySchema(schema = @Schema(description = "Идентификатор категории", example = "3"),
            arraySchema = @Schema(description = "Список идентификаторов категорий"))
    @NotEmpty(message = "Мероприятие должно иметь хотя бы одну категорию")
    private List<Long> categoryIds;
}
