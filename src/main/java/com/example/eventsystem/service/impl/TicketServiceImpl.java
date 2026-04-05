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
import com.example.eventsystem.model.entity.Ticket;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.TicketRepository;
import com.example.eventsystem.repository.UserRepository;
import com.example.eventsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository repository;
    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final TicketMapper mapper;

    @Override
    @Transactional
    public TicketResponseDto buyTicket(TicketRequestDto dto) {
        if (dto.getEventId() == null || dto.getUserId() == null) {
            throw new ValidationException("Event ID and User ID must not be null");
        }

        Event event = eventRepo.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        if (event.getTickets() != null && event.getTickets().size() >= event.getMaxParticipants()) {
            throw new ConflictException("No more tickets available for this event");
        }

        Ticket ticket = Ticket.builder()
                .event(event)
                .user(user)
                .barcode(dto.getBarcode())
                .purchaseDate(LocalDateTime.now())
                .build();

        Ticket savedTicket = repository.save(ticket);
        return mapper.toResponseDto(savedTicket);
    }

    @Override
    @Transactional
    public List<TicketResponseDto> buyTicketsBulkTransactional(BulkTicketRequestDto request) {
        return buyTicketsBulkInternal(request);
    }

    @Override
    public List<TicketResponseDto> buyTicketsBulkNonTransactional(BulkTicketRequestDto request) {
        return buyTicketsBulkInternal(request);
    }

    @Transactional(readOnly = true)
    public TicketResponseDto getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto> getTickets(Long userId, String barcode) {
        return repository.findAll().stream()
                .filter(t -> (userId == null || t.getUser().getId().equals(userId))
                        && (barcode == null || t.getBarcode().equals(barcode)))
                .map(mapper::toResponseDto).toList();
    }

    @Transactional
    public TicketResponseDto update(Long id, TicketRequestDto dto) {
        Ticket ticket = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        ticket.setBarcode(dto.getBarcode());
        return mapper.toResponseDto(repository.save(ticket));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Ticket not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> getAllTickets() {
        return repository.findAll().stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    private List<TicketResponseDto> buyTicketsBulkInternal(BulkTicketRequestDto request) {
        if (request == null || request.tickets() == null || request.tickets().isEmpty()) {
            throw new ValidationException("Tickets list must not be empty");
        }

        User user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Set<Long> eventIds = request.tickets().stream()
                .map(BulkTicketItemRequestDto::eventId)
                .collect(Collectors.toSet());

        Map<Long, Event> eventsById = eventRepo.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        Map<Long, Long> ticketsSoldByEvent = new HashMap<>();

        return request.tickets().stream()
                .map(ticketRequest -> buySingleTicketInBulk(
                        ticketRequest,
                        user,
                        eventsById,
                        ticketsSoldByEvent))
                .toList();
    }

    private TicketResponseDto buySingleTicketInBulk(BulkTicketItemRequestDto ticketRequest,
                                                    User defaultUser,
                                                    Map<Long, Event> eventsById,
                                                    Map<Long, Long> ticketsSoldByEvent) {
        Long eventId = Optional.ofNullable(ticketRequest.eventId())
                .orElseThrow(() -> new ValidationException("Event ID must not be null"));

        Event event = Optional.ofNullable(eventsById.get(eventId))
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User user = defaultUser;

        long soldBeforeBulk = ticketsSoldByEvent.computeIfAbsent(eventId, repository::countByEventId);
        if (soldBeforeBulk >= event.getMaxParticipants()) {
            throw new ConflictException("No more tickets available for event id: " + eventId);
        }

        Ticket ticket = Ticket.builder()
                .event(event)
                .user(user)
                .barcode(ticketRequest.barcode())
                .purchaseDate(LocalDateTime.now())
                .build();

        Ticket savedTicket = repository.save(ticket);
        ticketsSoldByEvent.put(eventId, soldBeforeBulk + 1);
        return mapper.toResponseDto(savedTicket);
    }
}
