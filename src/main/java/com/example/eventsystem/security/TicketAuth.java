package com.example.eventsystem.security;

import com.example.eventsystem.model.entity.Ticket;
import com.example.eventsystem.model.enums.AppRole;
import com.example.eventsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("ticketAuth")
@RequiredArgsConstructor
public class TicketAuth {

    private final TicketRepository ticketRepository;

    public boolean canAccessTicket(Long ticketId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null || ticket.getUser() == null) {
            return false;
        }
        return principal.getRole() == AppRole.USER && principal.getId().equals(ticket.getUser().getId());
    }

    private boolean isAdmin(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (AppRole.ADMIN.springAuthority().equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
