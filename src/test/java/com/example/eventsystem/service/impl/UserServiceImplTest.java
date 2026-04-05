package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.UserMapper;
import com.example.eventsystem.model.dto.UserRequestDto;
import com.example.eventsystem.model.dto.UserResponseDto;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Ivan Ivanov");
        user.setEmail("ivan@example.com");

        responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setFullName("Ivan Ivanov");
        responseDto.setEmail("ivan@example.com");
    }

    @Test
    void register_Success() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail("new@example.com");

        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.register(requestDto);

        assertNotNull(result);
        assertEquals("ivan@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getByEmail_Success() {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");

        when(userRepository.findAll()).thenReturn(List.of(otherUser, user));
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getByEmail("IVAN@example.com");

        assertNotNull(result);
        assertEquals("ivan@example.com", result.getEmail());
    }

    @Test
    void getByEmail_NotFound_ThrowsException() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertThrows(ResourceNotFoundException.class, () -> userService.getByEmail("nonexistent@example.com"));
    }

    @Test
    void update_Success() {
        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setFullName("New Name");
        updateDto.setEmail("new@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        userService.update(1L, updateDto);

        assertEquals("New Name", user.getFullName());
        assertEquals("new@email.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void delete_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}