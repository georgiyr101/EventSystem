package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные категории")
public class CategoryResponseDto {

    @Schema(description = "Идентификатор категории", example = "1")
    private Long id;

    @Schema(description = "Название категории", example = "Music")
    private String name;
}
