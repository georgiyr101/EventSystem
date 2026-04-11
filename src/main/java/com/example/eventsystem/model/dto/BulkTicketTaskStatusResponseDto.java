package com.example.eventsystem.model.dto;

import com.example.eventsystem.model.enums.BulkTicketTaskStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Bulk ticket async task status")
public record BulkTicketTaskStatusResponseDto(
        @Schema(description = "Async task identifier", example = "task-1")
        String taskId,

        @Schema(description = "Current async task status")
        BulkTicketTaskStatus status,

        @ArraySchema(arraySchema = @Schema(description = "Created tickets. Returned only when task is completed"))
        List<TicketResponseDto> result,

        @Schema(description = "Error details. Returned only when task failed",
                example = "No more tickets available for event id: 15")
        String error
) {
}
