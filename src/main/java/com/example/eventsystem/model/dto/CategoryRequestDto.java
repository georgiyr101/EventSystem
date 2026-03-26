package com.example.eventsystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto {

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 2, max = 50, message = "Название категории должно быть от 2 до 50 символов")
    private String name;
}