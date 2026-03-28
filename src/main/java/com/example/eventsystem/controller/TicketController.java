package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.service.TicketService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Ticket Controller", description = "Управление покупкой и проверкой билетов")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Купить билет",
            description = "Регистрирует покупку билета для пользователя на мероприятие")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Билет успешно куплен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации (например, отрицательная цена)"),
            @ApiResponse(responseCode = "404", description = "Пользователь или мероприятие не найдены")
    })
    @PostMapping
    public ResponseEntity<TicketResponseDto> buy(@Valid @RequestBody TicketRequestDto requestDto) {
        return new ResponseEntity<>(ticketService.buyTicket(requestDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить билет по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Билет найден"),
            @ApiResponse(responseCode = "404", description = "Билет с таким ID не существует")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @Operation(summary = "Поиск билетов")
    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String barcode) {

        if (userId == null && barcode == null) {
            return ResponseEntity.ok(ticketService.getAllTickets());
        }

        return ResponseEntity.ok(ticketService.getTickets(userId, barcode));
    }

    @Operation(summary = "Обновить данные билета")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные обновлены"),
            @ApiResponse(responseCode = "404", description = "Билет не найден"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody TicketRequestDto requestDto) {
        return ResponseEntity.ok(ticketService.update(id, requestDto));
    }

    @Operation(summary = "Аннулировать билет")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Билет удален/аннулирован"),
            @ApiResponse(responseCode = "404", description = "Билет не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
