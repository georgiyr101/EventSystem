package com.example.eventsystem.model.enums;

import lombok.Getter;

@Getter
public enum EventStatus {
    PLANNED("Запланировано"),
    ONGOING("Идет сейчас"),
    COMPLETED("Завершено"),
    CANCELLED("Отменено"),
    SOLD_OUT("Билеты проданы");

    private final String description;

    EventStatus(String description) {
        this.description = description;
    }

}