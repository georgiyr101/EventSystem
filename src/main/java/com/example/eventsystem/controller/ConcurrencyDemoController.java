package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.RaceConditionDemoResponse;
import com.example.eventsystem.service.ConcurrencyDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/concurrency")
@RequiredArgsConstructor
public class ConcurrencyDemoController {

    private final ConcurrencyDemoService service;

    @Operation(summary = "Demonstrate race condition and thread-safe solutions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demo completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/race-demo")
    public ResponseEntity<RaceConditionDemoResponse> runRaceDemo(
            @RequestParam(defaultValue = "60") @Min(1) final int threads,
            @RequestParam(defaultValue = "2000") @Min(1) final int incrementsPerThread
    ) {
        return ResponseEntity.ok(service.runRaceConditionDemo(threads, incrementsPerThread));
    }
}
