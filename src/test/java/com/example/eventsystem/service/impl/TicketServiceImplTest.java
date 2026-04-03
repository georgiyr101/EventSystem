package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ConflictException;
import com.example.eventsystem.exception.ValidationException;
import com.example.eventsystem.mapper.TicketMapper;
import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.entity.Ticket;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.TicketRepository;
import com.example.eventsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void buyTicketsBulkTransactional_shouldSaveAllTicketsWhenCapacityIsEnough() {
        User user = user(10L);
        Event event = event(100L, 5);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L,
                List.of(ticketRequest(100L, 10L, "AAA11111"),
                        ticketRequest(100L, 10L, "BBB22222")));

        stubCommonDependencies(user, event);

        List<TicketResponseDto> actual = ticketService.buyTicketsBulkTransactional(request);

        assertEquals(2, actual.size());
        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    @Test
    void buyTicketsBulkNonTransactional_shouldThrowWhenCapacityExceededAfterPartialSaves() {
        User user = user(10L);
        Event event = event(100L, 2);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L,
                List.of(
                        ticketRequest(100L, 10L, "AAA11111"),
                        ticketRequest(100L, 10L, "BBB22222"),
                        ticketRequest(100L, 10L, "CCC33333")
                ));

        stubCommonDependencies(user, event);

        assertThrows(ConflictException.class, () -> ticketService.buyTicketsBulkNonTransactional(request));

        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    @Test
    void buyTicketsBulkTransactional_shouldFailWhenUserIdInsideItemDiffersFromBulkUserId() {
        User user = user(10L);
        Event event = event(100L, 3);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L,
                List.of(ticketRequest(100L, 99L, "AAA11111")));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(eventRepository.findAllById(ArgumentMatchers.anyCollection())).thenReturn(List.of(event));

        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkTransactional(request));

        verify(ticketRepository, times(0)).save(any(Ticket.class));
        verifyNoMoreInteractions(ticketMapper);
    }

    private void stubCommonDependencies(User user, Event event) {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(eventRepository.findAllById(ArgumentMatchers.anyCollection())).thenReturn(List.of(event));
        when(ticketRepository.countByEventId(100L)).thenReturn(0L);

        AtomicLong idSeq = new AtomicLong(1L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(idSeq.getAndIncrement());
            return ticket;
        });
        when(ticketMapper.toResponseDto(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            TicketResponseDto dto = new TicketResponseDto();
            dto.setId(ticket.getId());
            dto.setEventId(ticket.getEvent().getId());
            dto.setUserId(ticket.getUser().getId());
            dto.setBarcode(ticket.getBarcode());
            return dto;
        });
    }

    private static TicketRequestDto ticketRequest(Long eventId, Long userId, String barcode) {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setEventId(eventId);
        dto.setUserId(userId);
        dto.setBarcode(barcode);
        return dto;
    }

    private static User user(Long id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .fullName("User")
                .build();
    }

    private static Event event(Long id, Integer maxParticipants) {
        return Event.builder()
                .id(id)
                .name("Event")
                .maxParticipants(maxParticipants)
                .build();
    }
}
