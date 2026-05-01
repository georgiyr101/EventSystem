package com.example.eventsystem.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Логин")
public class LoginRequestDto {

    @NotBlank
    @Email
    @Schema(example = "ivan@example.com")
    private String email;

    @NotBlank
    @Schema(example = "password123")
    private String password;
}
