package com.example.eventsystem.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    @NotBlank(message = "ФИО пользователя не может быть пустым")
    @Size(min = 2, max = 100, message = "ФИО должно быть от 2 до 100 символов")
    private String fullName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат электронной почты")
    private String email;
}