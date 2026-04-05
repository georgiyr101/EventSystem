package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Bulk ticket purchase request")
public record BulkTicketRequestDto(
        @Schema(description = "User identifier", example = "7")
        @NotNull(message = "User ID is required")
        Long userId,

        @ArraySchema(schema = @Schema(implementation = BulkTicketItemRequestDto.class),
                arraySchema = @Schema(description = "List of tickets to purchase"))
        @NotEmpty(message = "Tickets list must not be empty")
        @Valid
        List<BulkTicketItemRequestDto> tickets
) {
}
