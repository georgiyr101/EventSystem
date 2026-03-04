package com.example.eventsystem.service;

import com.example.eventsystem.model.dto.UserRequestDto;
import com.example.eventsystem.model.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto register(UserRequestDto dto);

    UserResponseDto getById(Long id);

    UserResponseDto getByEmail(String email);

    UserResponseDto update(Long id, UserRequestDto dto);

    void delete(Long id);

    List<UserResponseDto> getAllUsers();
}