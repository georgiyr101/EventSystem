package com.example.eventsystem.security;

import com.example.eventsystem.model.dto.EventRequestDto;
import com.example.eventsystem.model.enums.AppRole;
import com.example.eventsystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("eventAuth")
@RequiredArgsConstructor
public class EventAuth {

    private final EventRepository eventRepository;

    public boolean canCreate(EventRequestDto dto, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }
        if (principal.getRole() != AppRole.ORGANIZER) {
            return false;
        }
        return principal.getOrganizerId() != null && principal.getOrganizerId().equals(dto.getOrganizerId());
    }

    public boolean canMutateByEventId(Long eventId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }
        if (principal.getRole() != AppRole.ORGANIZER) {
            return false;
        }
        Long oid = principal.getOrganizerId();
        if (oid == null) {
            return false;
        }
        return eventRepository.existsByIdAndOrganizer_Id(eventId, oid);
    }

    public boolean canMutateWithOrganizerId(Long organizerId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }
        if (principal.getRole() != AppRole.ORGANIZER) {
            return false;
        }
        return principal.getOrganizerId() != null && principal.getOrganizerId().equals(organizerId);
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
