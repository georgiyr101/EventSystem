package com.example.eventsystem.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventRequestDto {
    @NotBlank(message = "Название мероприятия обязательно")
    private String name;

    @NotNull(message = "Дата начала обязательна")
    @Future(message = "Мероприятие должно быть в будущем")
    private LocalDateTime startDate;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDateTime endDate;

    @NotNull(message = "Максимальное количество участников обязательно")
    @Min(value = 1, message = "Количество участников должно быть не менее 1")
    private Integer maxParticipants;

    @NotNull(message = "Цена билета обязательна")
    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private Double ticketPrice;

    @NotNull(message = "ID организатора обязателен")
    private Long organizerId;

    @NotEmpty(message = "Мероприятие должно иметь хотя бы одну категорию")
    private java.util.List<Long> categoryIds;
}