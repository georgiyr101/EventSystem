package com.example.eventsystem.mapper;

import com.example.eventsystem.model.dto.OrganizerRequestDto;
import com.example.eventsystem.model.dto.OrganizerResponseDto;
import com.example.eventsystem.model.entity.Organizer;
import org.springframework.stereotype.Component;

@Component
public class OrganizerMapper {
    public Organizer toEntity(OrganizerRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Organizer.builder()
                .name(dto.getName())
                .contactInfo(dto.getContactInfo())
                .build();
    }

    public OrganizerResponseDto toResponseDto(Organizer organizer) {
        if (organizer == null) {
            return null;
        }
        return new OrganizerResponseDto(
                organizer.getId(),
                organizer.getName(),
                organizer.getContactInfo()
        );
    }
}