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
import com.example.eventsystem.model.enums.AppRole;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.TicketRepository;
import com.example.eventsystem.repository.UserRepository;
import com.example.eventsystem.security.UserPrincipal;
import com.example.eventsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (dto.getEventId() == null) {
            throw new ValidationException("Event ID must not be null");
        }

        Event event = eventRepo.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        User user = resolvePurchasingUser(dto.getUserId());

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppRole role = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            role = principal.getRole();
            if (principal.getRole() == AppRole.USER) {
                userId = principal.getId();
            } else if (principal.getRole() == AppRole.ORGANIZER) {
                throw new ValidationException("ORGANIZER cannot list tickets via this endpoint");
            }
        }

        if (userId == null && barcode == null) {
            if (role == AppRole.ADMIN) {
                return getAllTickets();
            }
            throw new ValidationException("userId or barcode must be provided");
        }

        final Long effectiveUserId = userId;
        return repository.findAll().stream()
                .filter(t -> (effectiveUserId == null || t.getUser().getId().equals(effectiveUserId))
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

    @Override
    @Transactional(readOnly = true)
    public long countByEventId(Long eventId) {
        return repository.countByEventId(eventId);
    }

    private List<TicketResponseDto> buyTicketsBulkInternal(BulkTicketRequestDto request) {
        if (request == null || request.tickets() == null || request.tickets().isEmpty()) {
            throw new ValidationException("Tickets list must not be empty");
        }

        User user = resolvePurchasingUser(request.userId());

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

    private User resolvePurchasingUser(Long requestedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ValidationException("Not authenticated");
        }

        if (principal.getRole() == AppRole.ADMIN) {
            Long userId = requestedUserId;
            if (userId == null) {
                throw new ValidationException("userId is required for ADMIN purchases");
            }
            return userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        }

        if (principal.getRole() == AppRole.USER) {
            return userRepo.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));
        }

        throw new ValidationException("Only USER or ADMIN can purchase tickets");
    }
}
