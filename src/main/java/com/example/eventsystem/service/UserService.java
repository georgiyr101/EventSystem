package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.UserRequestDto;
import com.example.eventsystem.model.dto.UserResponseDto;

public interface UserService {

    UserResponseDto register(UserRequestDto dto);

    UserResponseDto getById(Long id);
}