package com.example.eventsystem.model.dto.auth;

import com.example.eventsystem.model.enums.AppRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ аутентификации")
public class AuthResponseDto {

    @Schema(example = "Bearer")
    private String tokenType;

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "User id")
    private Long userId;

    @Schema(example = "ivan@example.com")
    private String email;

    @Schema(example = "Ivan Petrov")
    private String fullName;

    @Schema(example = "USER")
    private AppRole role;

    @Schema(description = "Organizer profile id (only for ORGANIZER)")
    private Long organizerId;
}
