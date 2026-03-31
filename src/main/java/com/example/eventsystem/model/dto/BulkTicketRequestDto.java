package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Запрос на пакетную покупку билетов")
public record BulkTicketRequestDto(
        @Schema(description = "Идентификатор пользователя", example = "7")
        @NotNull(message = "User ID обязателен")
        Long userId,

        @ArraySchema(schema = @Schema(implementation = TicketRequestDto.class),
                arraySchema = @Schema(description = "Список билетов для покупки"))
        @NotEmpty(message = "Список билетов не может быть пустым")
        @Valid
        List<TicketRequestDto> tickets
) {

}
