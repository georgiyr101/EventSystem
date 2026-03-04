package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Добавление билета на мероприятие.
     * POST /api/v1/tickets
     */
    @PostMapping
    public ResponseEntity<TicketResponseDto> buy(@RequestBody TicketRequestDto requestDto) {
        return new ResponseEntity<>(ticketService.buyTicket(requestDto), HttpStatus.CREATED);
    }

    /**
     * Получение информации о билете по его ID.
     * GET /api/v1/tickets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String barcode) {

        if (userId == null && barcode == null) {
            return ResponseEntity.ok(ticketService.getAllTickets());
        }

        return ResponseEntity.ok(ticketService.getTickets(userId, barcode));
    }

    /**
     * Обновление данных билета (например, изменение штрих-кода).
     * PUT /api/v1/tickets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDto> update(@PathVariable Long id, @RequestBody TicketRequestDto requestDto) {
        return ResponseEntity.ok(ticketService.update(id, requestDto));
    }

    /**
     * Аннулирование/удаление билета.
     * DELETE /api/v1/tickets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
