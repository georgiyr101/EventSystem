package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.TicketMapper;
import com.example.eventsystem.model.dto.TicketRequestDto;
import com.example.eventsystem.model.dto.TicketResponseDto;
import com.example.eventsystem.model.entity.Event;
import com.example.eventsystem.model.entity.Ticket;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.repository.EventRepository;
import com.example.eventsystem.repository.TicketRepository;
import com.example.eventsystem.repository.UserRepository;
import com.example.eventsystem.service.TicketService;
import com.example.eventsystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
            throw new IllegalArgumentException("Event ID and User ID must not be null");
        }

        Event event = eventRepo.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        Ticket ticket = Ticket.builder()
                .event(event)
                .user(user)
                .barcode(dto.getBarcode())
                .purchaseDate(LocalDateTime.now())
                .build();

        Ticket savedTicket = repository.save(ticket);
        return mapper.toResponseDto(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponseDto getById(Long id) {
        return repository.findById(id).map(mapper::toResponseDto).orElseThrow();
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
        Ticket ticket = repository.findById(id).orElseThrow();
        ticket.setBarcode(dto.getBarcode());
        return mapper.toResponseDto(repository.save(ticket));
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> getAllTickets() {
        return repository.findAll().stream()
                .map(mapper::toResponseDto)
                .toList();
    }
}