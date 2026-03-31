package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные организатора")
public class OrganizerResponseDto {

    @Schema(description = "Идентификатор организатора", example = "10")
    private Long id;

    @Schema(description = "Имя организатора", example = "Tech Community")
    private String name;

    @Schema(description = "Контактный email организатора", example = "contact@tech-community.by")
    private String contactInfo;
}
