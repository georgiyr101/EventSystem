package com.example.eventsystem.model.dto.auth;

import com.example.eventsystem.model.enums.AppRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Регистрация пользователя")
public class RegisterRequestDto {

    @NotBlank
    @Email
    @Schema(example = "ivan@example.com")
    private String email;

    @NotBlank
    @Size(min = 2, max = 120)
    @Schema(example = "Ivan Petrov")
    private String fullName;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(example = "password123")
    private String password;

    @NotNull
    @Schema(example = "USER", allowableValues = {"USER", "ORGANIZER"})
    private AppRole role;
}
