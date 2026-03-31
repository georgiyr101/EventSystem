package com.example.eventsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания или обновления организатора")
public class OrganizerRequestDto {

    @Schema(description = "Имя организатора", example = "Tech Community")
    @NotBlank(message = "Имя организатора не может быть пустым")
    private String name;

    @Schema(description = "Контактный email организатора", example = "contact@tech-community.by")
    @Email(message = "Некорректный формат электронной почты")
    @NotBlank(message = "Контактная информация обязательна")
    private String contactInfo;
}
