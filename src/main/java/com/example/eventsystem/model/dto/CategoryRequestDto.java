package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания или обновления категории")
public class CategoryRequestDto {

    @Schema(description = "Название категории", example = "Music")
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 2, max = 50, message = "Название категории должно быть от 2 до 50 символов")
    private String name;
}
