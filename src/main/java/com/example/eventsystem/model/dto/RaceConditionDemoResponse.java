package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Race condition demonstration result")
public class RaceConditionDemoResponse {

    @Schema(description = "Actual number of threads used", example = "60")
    private int threads;

    @Schema(description = "Increments performed by each thread", example = "2000")
    private int incrementsPerThread;

    @Schema(description = "Expected final value", example = "120000")
    private int expected;

    @Schema(description = "Result from non-thread-safe counter", example = "83541")
    private int unsafeResult;

    @Schema(description = "Result from synchronized counter", example = "120000")
    private int synchronizedResult;

    @Schema(description = "Result from atomic counter", example = "120000")
    private int atomicResult;
}
