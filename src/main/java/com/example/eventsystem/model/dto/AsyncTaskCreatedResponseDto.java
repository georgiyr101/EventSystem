package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Created async task response")
public record AsyncTaskCreatedResponseDto(
        @Schema(description = "Async task identifier", example = "task-1")
        String taskId
) {
}
