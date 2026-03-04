package com.example.eventsystem.service.impl;

import com.example.eventsystem.mapper.UserMapper;
import com.example.eventsystem.model.dto.UserRequestDto;
import com.example.eventsystem.model.dto.UserResponseDto;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.repository.UserRepository;
import com.example.eventsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto register(UserRequestDto dto) {
        User user = userMapper.toEntity(dto);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}