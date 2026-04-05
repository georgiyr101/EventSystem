package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ConflictException;
import com.example.eventsystem.model.dto.BulkTicketItemRequestDto;
import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.model.enums.EventStatus;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.TicketRepository;
import com.example.eventsystem.repository.UserRepository;
import com.example.eventsystem.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class TicketBulkTransactionIntegrationTest {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        ticketRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void bulkNonTransactional_shouldLeavePartialDataWhenExceptionOccurs() {
        User user = userRepository.save(User.builder().email("bulk-non-tx@example.com").fullName("Bulk User").build());
        Event event = eventRepository.save(Event.builder()
                .name("Conference")
                .status(EventStatus.PLANNED)
                .maxParticipants(2)
                .ticketPrice(50.0)
                .build());

        BulkTicketRequestDto request = new BulkTicketRequestDto(user.getId(), List.of(
                bulkItem(event.getId(), "BULKNON1"),
                bulkItem(event.getId(), "BULKNON2"),
                bulkItem(event.getId(), "BULKNON3")
        ));

        assertThrows(ConflictException.class, () -> ticketService.buyTicketsBulkNonTransactional(request));

        assertEquals(2L, ticketRepository.countByEventId(event.getId()));
    }

    @Test
    void bulkTransactional_shouldRollbackAllDataWhenExceptionOccurs() {
        User user = userRepository.save(User.builder().email("bulk-tx@example.com").fullName("Bulk User").build());
        Event event = eventRepository.save(Event.builder()
                .name("Meetup")
                .status(EventStatus.PLANNED)
                .maxParticipants(2)
                .ticketPrice(30.0)
                .build());

        BulkTicketRequestDto request = new BulkTicketRequestDto(user.getId(), List.of(
                bulkItem(event.getId(), "BULKTX01"),
                bulkItem(event.getId(), "BULKTX02"),
                bulkItem(event.getId(), "BULKTX03")
        ));

        assertThrows(ConflictException.class, () -> ticketService.buyTicketsBulkTransactional(request));

        assertEquals(0L, ticketRepository.countByEventId(event.getId()));
    }

    private static BulkTicketItemRequestDto bulkItem(Long eventId, String barcode) {
        return new BulkTicketItemRequestDto(eventId, barcode);
    }
}
