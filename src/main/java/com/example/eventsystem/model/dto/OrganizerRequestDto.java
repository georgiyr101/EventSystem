package com.example.eventsystem.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerRequestDto {

    @NotBlank(message = "Имя организатора не может быть пустым")
    private String name;

    @Email(message = "Некорректный формат электронной почты")
    @NotBlank(message = "Контактная информация обязательна")
    private String contactInfo;
}