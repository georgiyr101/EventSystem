package com.example.eventsystem.mapper;

import com.example.eventsystem.model.dto.UserRequestDto;
import com.example.eventsystem.model.dto.UserResponseDto;
import com.example.eventsystem.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .build();
    }

    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(user.getId(), user.getFullName(), user.getEmail());
    }
}