package com.example.eventsystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestDto {
    @NotNull(message = "Event ID обязателен")
    private Long eventId;

    @NotNull(message = "User ID обязателен")
    private Long userId;

    @NotBlank(message = "Штрих-код не может быть пустым")
    @Pattern(regexp = "^[A-Z0-9-]{8,20}$", message = "Штрих-код должен состоять из 8-20 заглавных букв, цифр и дефиса")
    private String barcode;
}
