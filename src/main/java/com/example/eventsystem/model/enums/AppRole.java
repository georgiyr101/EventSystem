package com.example.eventsystem.model.enums;

public enum AppRole {
    USER,
    ORGANIZER,
    ADMIN;

    public String springAuthority() {
        return "ROLE_" + name();
    }
}
