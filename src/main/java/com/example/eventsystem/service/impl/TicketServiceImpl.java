package com.example.eventsystem.service.impl;

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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public TicketResponseDto buyTicket(TicketRequestDto dto) {

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Ticket ticket = Ticket.builder()
                .event(event)
                .user(user)
                .barcode(dto.getBarcode())
                .purchaseDate(java.time.LocalDateTime.now())
                .build();

        return ticketMapper.toResponseDto(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDto> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(ticketMapper::toResponseDto)
                .toList();
    }
}