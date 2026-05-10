package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ConflictException;
import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.exception.ValidationException;
import com.example.eventsystem.mapper.TicketMapper;
import com.example.eventsystem.model.dto.BulkTicketItemRequestDto;
import com.example.eventsystem.model.dto.BulkTicketRequestDto;
import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.model.entity.Ticket;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.model.enums.AppRole;
import com.example.eventsystem.model.enums.EventStatus;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.TicketRepository;
import com.example.eventsystem.repository.UserRepository;
import com.example.eventsystem.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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
    void buyTicket_shouldSaveAndMap() {
        loginAsUser(10L);
        TicketRequestDto request = ticketRequest(100L, 10L, "AAA11111");
        Event event = event(100L, 2);
        event.setTickets(new ArrayList<>());
        User user = user(10L);
        TicketResponseDto response = new TicketResponseDto();
        response.setId(1L);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(ticketMapper.toResponseDto(any(Ticket.class))).thenReturn(response);

        TicketResponseDto actual = ticketService.buyTicket(request);

        assertEquals(1L, actual.getId());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void buyTicket_shouldThrowValidationWhenIdsAreNull() {
        TicketRequestDto request = new TicketRequestDto();
        request.setBarcode("AAA11111");

        assertThrows(ValidationException.class, () -> ticketService.buyTicket(request));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void buyTicket_shouldThrowValidationWhenOnlyUserIdIsNull() {
        loginAsUser(10L);
        TicketRequestDto request = new TicketRequestDto();
        request.setEventId(100L);
        request.setUserId(null);
        request.setBarcode("AAA11111");

        Event event = event(100L, 2);
        event.setTickets(new ArrayList<>());
        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketMapper.toResponseDto(any(Ticket.class))).thenReturn(new TicketResponseDto());

        ticketService.buyTicket(request);
        verify(userRepository).findById(10L);
    }

    @Test
    void buyTicket_shouldThrowValidationWhenOnlyEventIdIsNull() {
        TicketRequestDto request = new TicketRequestDto();
        request.setEventId(null);
        request.setUserId(10L);
        request.setBarcode("AAA11111");

        assertThrows(ValidationException.class, () -> ticketService.buyTicket(request));
    }

    @Test
    void buyTicket_shouldThrowConflictWhenEventIsSoldOut() {
        loginAsUser(10L);
        TicketRequestDto request = ticketRequest(100L, 10L, "AAA11111");
        Event event = event(100L, 1);
        event.setTickets(new ArrayList<>(List.of(new Ticket())));

        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));

        assertThrows(ConflictException.class, () -> ticketService.buyTicket(request));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void buyTicket_shouldThrowWhenEventNotFound() {
        loginAsUser(10L);
        TicketRequestDto request = ticketRequest(100L, 10L, "AAA11111");
        when(eventRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.buyTicket(request));
    }

    @Test
    void buyTicket_shouldThrowWhenUserNotFound() {
        loginAsUser(10L);
        TicketRequestDto request = ticketRequest(100L, 10L, "AAA11111");
        Event event = event(100L, 10);
        event.setTickets(new ArrayList<>());

        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.buyTicket(request));
    }

    @Test
    void buyTicket_shouldAllowWhenEventTicketsCollectionIsNull() {
        loginAsUser(10L);
        TicketRequestDto request = ticketRequest(100L, 10L, "AAA11111");
        Event event = event(100L, 10);
        event.setTickets(null);
        User user = user(10L);
        TicketResponseDto response = new TicketResponseDto();
        response.setId(77L);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketMapper.toResponseDto(any(Ticket.class))).thenReturn(response);

        TicketResponseDto actual = ticketService.buyTicket(request);

        assertEquals(77L, actual.getId());
    }

    @Test
    void getById_shouldReturnMappedTicket() {
        Ticket ticket = Ticket.builder().id(1L).build();
        TicketResponseDto response = new TicketResponseDto();
        response.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponseDto(ticket)).thenReturn(response);

        TicketResponseDto actual = ticketService.getById(1L);

        assertEquals(1L, actual.getId());
    }

    @Test
    void getById_shouldThrowWhenMissing() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.getById(99L));
    }

    @Test
    void getTickets_shouldFilterByUserAndBarcode() {
        loginAsUser(10L);
        User u1 = user(10L);
        User u2 = user(20L);
        Event event = event(100L, 10);
        Ticket t1 = Ticket.builder().id(1L).user(u1).event(event).barcode("AAA11111").build();
        Ticket t2 = Ticket.builder().id(2L).user(u1).event(event).barcode("BBB22222").build();
        Ticket t3 = Ticket.builder().id(3L).user(u2).event(event).barcode("AAA11111").build();
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(1L);

        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2, t3));
        when(ticketMapper.toResponseDto(t1)).thenReturn(dto);

        List<TicketResponseDto> result = ticketService.getTickets(10L, "AAA11111");

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void getTickets_shouldReturnAllWhenFiltersAreNull() {
        loginAsAdmin();
        User u1 = user(10L);
        Event event = event(100L, 10);
        Ticket t1 = Ticket.builder().id(1L).user(u1).event(event).barcode("AAA11111").build();
        Ticket t2 = Ticket.builder().id(2L).user(u1).event(event).barcode("BBB22222").build();
        TicketResponseDto d1 = new TicketResponseDto();
        d1.setId(1L);
        TicketResponseDto d2 = new TicketResponseDto();
        d2.setId(2L);

        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2));
        when(ticketMapper.toResponseDto(t1)).thenReturn(d1);
        when(ticketMapper.toResponseDto(t2)).thenReturn(d2);

        List<TicketResponseDto> result = ticketService.getTickets(null, null);

        assertEquals(2, result.size());
    }

    @Test
    void getTickets_shouldThrowWhenOrganizerCallsListEndpoint() {
        loginAsOrganizer(1L);

        assertThrows(ValidationException.class, () -> ticketService.getTickets(10L, null));
    }

    @Test
    void getTickets_shouldThrowWhenUnauthenticatedAndNoFilters() {
        SecurityContextHolder.clearContext();

        assertThrows(ValidationException.class, () -> ticketService.getTickets(null, null));
    }

    @Test
    void getTickets_shouldUsePrincipalUserIdForUserRole() {
        loginAsUser(10L);
        User u10 = user(10L);
        User u20 = user(20L);
        Event event = event(100L, 10);
        Ticket t10 = Ticket.builder().id(1L).user(u10).event(event).barcode("A").build();
        Ticket t20 = Ticket.builder().id(2L).user(u20).event(event).barcode("B").build();
        TicketResponseDto d1 = new TicketResponseDto();
        d1.setId(1L);

        when(ticketRepository.findAll()).thenReturn(List.of(t10, t20));
        when(ticketMapper.toResponseDto(t10)).thenReturn(d1);

        List<TicketResponseDto> result = ticketService.getTickets(999L, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void countByEventId_shouldDelegateToRepository() {
        when(ticketRepository.countByEventId(42L)).thenReturn(7L);

        assertEquals(7L, ticketService.countByEventId(42L));
    }

    @Test
    void getTickets_shouldFilterByBarcodeOnlyForAdmin() {
        loginAsAdmin();
        User u10 = user(10L);
        Event event = event(100L, 10);
        Ticket t1 = Ticket.builder().id(1L).user(u10).event(event).barcode("MATCH").build();
        Ticket t2 = Ticket.builder().id(2L).user(u10).event(event).barcode("OTHER").build();
        TicketResponseDto d1 = new TicketResponseDto();
        d1.setId(1L);

        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2));
        when(ticketMapper.toResponseDto(t1)).thenReturn(d1);

        List<TicketResponseDto> result = ticketService.getTickets(null, "MATCH");

        assertEquals(1, result.size());
    }

    @Test
    void buyTicketsBulkTransactional_shouldThrowWhenAdminUserIdMissing() {
        loginAsAdmin();
        BulkTicketRequestDto request = new BulkTicketRequestDto(null, List.of(bulkItem(100L, "A")));

        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkTransactional(request));
    }

    @Test
    void buyTicketsBulkTransactional_shouldThrowWhenAdminUserNotFound() {
        loginAsAdmin();
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L, List.of(bulkItem(100L, "A")));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.buyTicketsBulkTransactional(request));
    }

    @Test
    void buyTicketsBulkTransactional_shouldThrowWhenOrganizerTriesBulk() {
        loginAsOrganizer(1L);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L, List.of(bulkItem(100L, "A")));

        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkTransactional(request));
    }

    @Test
    void buyTicketsBulkTransactional_shouldThrowWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L, List.of(bulkItem(100L, "A")));

        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkTransactional(request));
    }

    @Test
    void buyTicketsBulkTransactional_shouldThrowWhenAuthenticationPrincipalIsNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(null, null, Collections.emptyList()));
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L, List.of(bulkItem(100L, "A")));

        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkTransactional(request));
    }

    @Test
    void buyTicketsBulkTransactional_shouldThrowWhenUserPrincipalHasNoUserRow() {
        loginAsUser(10L);
        BulkTicketRequestDto request = new BulkTicketRequestDto(null, List.of(bulkItem(100L, "A")));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.buyTicketsBulkTransactional(request));
    }

    @Test
    void buyTicketsBulkTransactional_shouldThrowWhenEventMissingFromRepository() {
        loginAsAdmin();
        User user = user(10L);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L, List.of(bulkItem(100L, "A")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(eventRepository.findAllById(ArgumentMatchers.anyCollection())).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.buyTicketsBulkTransactional(request));
    }

    @Test
    void delete_shouldThrowValidationWhenTicketHasNoEvent() {
        loginAsUser(10L);
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(null).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        assertThrows(ValidationException.class, () -> ticketService.delete(5L));
    }

    @Test
    void delete_shouldThrowConflictWhenEventCompletedForUser() {
        loginAsUser(10L);
        Event ev = event(100L, 10);
        ev.setStatus(EventStatus.COMPLETED);
        ev.setStartDate(LocalDateTime.now().plusDays(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(ev).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () -> ticketService.delete(5L));
    }

    @Test
    void delete_shouldThrowConflictWhenEventOngoingForUser() {
        loginAsUser(10L);
        Event ev = event(100L, 10);
        ev.setStatus(EventStatus.ONGOING);
        ev.setStartDate(LocalDateTime.now().plusDays(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(ev).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () -> ticketService.delete(5L));
    }

    @Test
    void delete_shouldSucceedWhenEventStartDateNull() {
        loginAsUser(10L);
        Event ev = event(100L, 10);
        ev.setStatus(EventStatus.PLANNED);
        ev.setStartDate(null);
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(ev).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        ticketService.delete(5L);

        verify(ticketRepository).deleteById(5L);
    }

    @Test
    void delete_shouldValidateWhenAuthenticationPrincipalIsNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(null, null, Collections.emptyList()));
        Event ev = event(100L, 10);
        ev.setStatus(EventStatus.PLANNED);
        ev.setStartDate(LocalDateTime.now().plusDays(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(ev).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        ticketService.delete(5L);

        verify(ticketRepository).deleteById(5L);
    }

    @Test
    void delete_shouldValidateWhenPrincipalIsOrganizer() {
        loginAsOrganizer(1L);
        Event ev = event(100L, 10);
        ev.setStatus(EventStatus.PLANNED);
        ev.setStartDate(LocalDateTime.now().plusDays(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(ev).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        ticketService.delete(5L);

        verify(ticketRepository).deleteById(5L);
    }

    @Test
    void delete_shouldTreatNonUserPrincipalAsNonAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null, List.of()));
        Event ev = event(100L, 10);
        ev.setStatus(EventStatus.PLANNED);
        ev.setStartDate(LocalDateTime.now().plusDays(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(ev).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        ticketService.delete(5L);

        verify(ticketRepository).deleteById(5L);
    }

    @Test
    void getTickets_shouldWorkWhenAuthenticationHasNonUserPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("svc", null, List.of()));
        User u10 = user(10L);
        Event event = event(100L, 10);
        Ticket t1 = Ticket.builder().id(1L).user(u10).event(event).barcode("MATCH").build();
        TicketResponseDto d1 = new TicketResponseDto();
        d1.setId(1L);

        when(ticketRepository.findAll()).thenReturn(List.of(t1));
        when(ticketMapper.toResponseDto(t1)).thenReturn(d1);

        List<TicketResponseDto> result = ticketService.getTickets(10L, "MATCH");

        assertEquals(1, result.size());
    }

    @Test
    void getTickets_shouldFilterOnlyByUserWhenBarcodeIsNull() {
        loginAsUser(10L);
        User u1 = user(10L);
        User u2 = user(20L);
        Event event = event(100L, 10);
        Ticket t1 = Ticket.builder().id(1L).user(u1).event(event).barcode("AAA11111").build();
        Ticket t2 = Ticket.builder().id(2L).user(u2).event(event).barcode("BBB22222").build();
        TicketResponseDto d1 = new TicketResponseDto();
        d1.setId(1L);

        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2));
        when(ticketMapper.toResponseDto(t1)).thenReturn(d1);

        List<TicketResponseDto> result = ticketService.getTickets(10L, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void update_shouldChangeBarcode() {
        Ticket ticket = Ticket.builder().id(1L).barcode("OLD11111").build();
        TicketRequestDto dto = ticketRequest(100L, 10L, "NEW11111");
        TicketResponseDto response = new TicketResponseDto();
        response.setBarcode("NEW11111");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponseDto(ticket)).thenReturn(response);

        TicketResponseDto actual = ticketService.update(1L, dto);

        assertEquals("NEW11111", ticket.getBarcode());
        assertEquals("NEW11111", actual.getBarcode());
    }

    @Test
    void update_shouldThrowWhenTicketMissing() {
        when(ticketRepository.findById(11L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.update(11L, ticketRequest(1L, 1L, "AAA11111")));
    }

    @Test
    void delete_shouldDeleteWhenExistsForUserBeforeEventStart() {
        loginAsUser(10L);
        Event event = event(100L, 10);
        event.setStatus(EventStatus.PLANNED);
        event.setStartDate(LocalDateTime.now().plusDays(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(event).build();

        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        ticketService.delete(5L);

        verify(ticketRepository).deleteById(5L);
    }

    @Test
    void delete_shouldThrowWhenMissing() {
        loginAsUser(10L);
        when(ticketRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.delete(5L));
        verify(ticketRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldThrowConflictWhenEventAlreadyStartedForUser() {
        loginAsUser(10L);
        Event event = event(100L, 10);
        event.setStatus(EventStatus.PLANNED);
        event.setStartDate(LocalDateTime.now().minusHours(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(event).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () -> ticketService.delete(5L));
        verify(ticketRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldAllowAdminDespiteStartedEvent() {
        loginAsAdmin();
        Event event = event(100L, 10);
        event.setStatus(EventStatus.PLANNED);
        event.setStartDate(LocalDateTime.now().minusHours(1));
        Ticket ticket = Ticket.builder().id(5L).user(user(10L)).event(event).build();
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        ticketService.delete(5L);

        verify(ticketRepository).deleteById(5L);
    }

    @Test
    void getAllTickets_shouldMapAll() {
        Ticket t1 = Ticket.builder().id(1L).build();
        Ticket t2 = Ticket.builder().id(2L).build();
        TicketResponseDto d1 = new TicketResponseDto();
        d1.setId(1L);
        TicketResponseDto d2 = new TicketResponseDto();
        d2.setId(2L);

        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2));
        when(ticketMapper.toResponseDto(t1)).thenReturn(d1);
        when(ticketMapper.toResponseDto(t2)).thenReturn(d2);

        List<TicketResponseDto> actual = ticketService.getAllTickets();

        assertEquals(2, actual.size());
        assertNotNull(actual.getFirst());
    }

    @Test
    void buyTicketsBulkTransactional_shouldSaveAllTicketsWhenCapacityIsEnough() {
        loginAsAdmin();
        User user = user(10L);
        Event event = event(100L, 5);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L,
                List.of(bulkItem(100L, "AAA11111"),
                        bulkItem(100L, "BBB22222")));

        stubCommonDependencies(user, event);

        List<TicketResponseDto> actual = ticketService.buyTicketsBulkTransactional(request);

        assertEquals(2, actual.size());
        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    @Test
    void buyTicketsBulkNonTransactional_shouldThrowWhenCapacityExceededAfterPartialSaves() {
        loginAsAdmin();
        User user = user(10L);
        Event event = event(100L, 2);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L,
                List.of(
                        bulkItem(100L, "AAA11111"),
                        bulkItem(100L, "BBB22222"),
                        bulkItem(100L, "CCC33333")
                ));

        stubCommonDependencies(user, event);

        assertThrows(ConflictException.class, () -> ticketService.buyTicketsBulkNonTransactional(request));

        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    @Test
    void buyTicketsBulkNonTransactional_shouldSaveWhenRequestIsValid() {
        loginAsAdmin();
        User user = user(10L);
        Event event = event(100L, 5);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L,
                List.of(bulkItem(100L, "AAA11111")));

        stubCommonDependencies(user, event);

        List<TicketResponseDto> actual = ticketService.buyTicketsBulkNonTransactional(request);

        assertEquals(1, actual.size());
    }

    @Test
    void buyTicketsBulkNonTransactional_shouldThrowValidationWhenRequestIsNull() {
        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkNonTransactional(null));
    }

    @Test
    void buyTicketsBulkNonTransactional_shouldThrowValidationWhenTicketsListIsNull() {
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L, null);
        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkNonTransactional(request));
    }

    @Test
    void buyTicketsBulkNonTransactional_shouldThrowValidationWhenTicketsListIsEmpty() {
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L, List.of());
        assertThrows(ValidationException.class, () -> ticketService.buyTicketsBulkNonTransactional(request));
    }

    @Test
    void buyTicketsBulkTransactional_shouldFailWhenEventIdInsideItemIsNull() {
        loginAsAdmin();
        User user = user(10L);
        Event event = event(100L, 3);
        BulkTicketRequestDto request = new BulkTicketRequestDto(10L,
                List.of(bulkItem(null, "AAA11111")));

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

    private static BulkTicketItemRequestDto bulkItem(Long eventId, String barcode) {
        return new BulkTicketItemRequestDto(eventId, barcode);
    }

    private static User user(Long id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .fullName("User")
                .role(AppRole.USER)
                .build();
    }

    private static Event event(Long id, Integer maxParticipants) {
        return Event.builder()
                .id(id)
                .name("Event")
                .maxParticipants(maxParticipants)
                .build();
    }

    private void loginAsUser(long userId) {
        User entity = user(userId);
        UserPrincipal principal = new UserPrincipal(entity);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private void loginAsAdmin() {
        User entity = User.builder()
                .id(1L)
                .email("admin@example.com")
                .fullName("Admin")
                .role(AppRole.ADMIN)
                .build();
        UserPrincipal principal = new UserPrincipal(entity);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private void loginAsOrganizer(long organizerProfileId) {
        Organizer profile = Organizer.builder().id(organizerProfileId).name("Org").build();
        User entity = User.builder()
                .id(2L)
                .email("org@example.com")
                .role(AppRole.ORGANIZER)
                .organizerProfile(profile)
                .build();
        UserPrincipal principal = new UserPrincipal(entity);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
