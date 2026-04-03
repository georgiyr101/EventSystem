package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Controller", description = "Ticket purchase and verification management")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Buy ticket",
            description = "Registers ticket purchase for a user and event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket purchased"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User or event not found")
    })
    @PostMapping
    public ResponseEntity<TicketResponseDto> buy(@Valid @RequestBody TicketRequestDto requestDto) {
        return new ResponseEntity<>(ticketService.buyTicket(requestDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Bulk ticket purchase",
            description = "Purchases multiple tickets in a single request. "
                    + "With transactional=true operation is atomic, otherwise partial writes are possible.")
    @PostMapping("/bulk")
    public ResponseEntity<List<TicketResponseDto>> buyBulk(@Valid @RequestBody BulkTicketRequestDto requestDto,
                                                           @RequestParam(defaultValue = "true") boolean transactional) {
        List<TicketResponseDto> result = transactional
                ? ticketService.buyTicketsBulkTransactional(requestDto)
                : ticketService.buyTicketsBulkNonTransactional(requestDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Get ticket by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket found"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @Operation(summary = "Search tickets")
    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String barcode) {

        if (userId == null && barcode == null) {
            return ResponseEntity.ok(ticketService.getAllTickets());
        }

        return ResponseEntity.ok(ticketService.getTickets(userId, barcode));
    }

    @Operation(summary = "Update ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket updated"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody TicketRequestDto requestDto) {
        return ResponseEntity.ok(ticketService.update(id, requestDto));
    }

    @Operation(summary = "Delete ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ticket deleted"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
