package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для регистрации или обновления пользователя")
public class UserRequestDto {

    @Schema(description = "Полное имя пользователя", example = "Ivan Petrov")
    @NotBlank(message = "ФИО пользователя не может быть пустым")
    @Size(min = 2, max = 100, message = "ФИО должно быть от 2 до 100 символов")
    private String fullName;

    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com")
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат электронной почты")
    private String email;
}
