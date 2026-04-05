package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Ticket item for bulk purchase")
public record BulkTicketItemRequestDto(
        @Schema(description = "Event identifier", example = "15")
        @NotNull(message = "Event ID is required")
        Long eventId,

        @Schema(description = "Ticket barcode", example = "ABC12345-01")
        @NotBlank(message = "Barcode must not be blank")
        @Pattern(regexp = "^[A-Z0-9-]{8,20}$",
                message = "Barcode must contain 8-20 uppercase letters, digits or hyphen")
        String barcode
) {
}
