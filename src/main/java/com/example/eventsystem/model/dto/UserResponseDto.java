package com.example.eventsystem.model.dto;

import com.example.eventsystem.model.enums.AppRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные пользователя")
public class UserResponseDto {

    @Schema(description = "Идентификатор пользователя", example = "7")
    private Long id;

    @Schema(description = "Полное имя пользователя", example = "Ivan Petrov")
    private String fullName;

    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com")
    private String email;

    @Schema(description = "Роль пользователя", example = "USER")
    private AppRole role;

    @Schema(description = "ID профиля организатора (если применимо)", example = "10")
    private Long organizerId;
}
