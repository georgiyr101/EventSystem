package com.example.eventsystem.model.dto.auth;

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
@Schema(description = "Обновление профиля текущего пользователя")
public class ProfileUpdateRequestDto {

    @NotBlank
    @Schema(example = "Иван Иванов")
    private String fullName;

    @NotBlank
    @Email
    @Schema(example = "user@example.com")
    private String email;

    @Schema(description = "Текущий пароль; обязателен, если задаётся новый пароль")
    private String currentPassword;

    @Size(min = 8)
    @Schema(description = "Новый пароль; опционально")
    private String newPassword;
}
