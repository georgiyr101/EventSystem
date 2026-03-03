package com.example.eventsystem.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerResponseDto {
    private Long id;
    private String name;
    private String contactInfo;
}
