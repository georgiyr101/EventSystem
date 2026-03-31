package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для покупки или обновления билета")
public class TicketRequestDto {

    @Schema(description = "Идентификатор мероприятия", example = "15")
    @NotNull(message = "Event ID обязателен")
    private Long eventId;

    @Schema(description = "Идентификатор пользователя", example = "7")
    @NotNull(message = "User ID обязателен")
    private Long userId;

    @Schema(description = "Штрих-код билета", example = "ABC12345-01")
    @NotBlank(message = "Штрих-код не может быть пустым")
    @Pattern(regexp = "^[A-Z0-9-]{8,20}$",
            message = "Штрих-код должен состоять из 8-20 заглавных букв, цифр и дефиса")
    private String barcode;
}
