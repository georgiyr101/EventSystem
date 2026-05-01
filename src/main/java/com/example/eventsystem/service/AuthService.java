package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.auth.AuthResponseDto;
import com.example.eventsystem.model.dto.auth.LoginRequestDto;
import com.example.eventsystem.model.dto.auth.ProfileUpdateRequestDto;
import com.example.eventsystem.model.dto.auth.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto dto);

    AuthResponseDto login(LoginRequestDto dto);

    AuthResponseDto me();

    AuthResponseDto updateProfile(ProfileUpdateRequestDto dto);
}
