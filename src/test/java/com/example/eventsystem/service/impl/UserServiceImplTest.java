package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.mapper.UserMapper;
import com.example.eventsystem.model.dto.UserRequestDto;
import com.example.eventsystem.model.dto.UserResponseDto;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_shouldSaveUser() {
        UserRequestDto dto = new UserRequestDto("Ivan", "ivan@example.com");
        User user = User.builder().fullName("Ivan").email("ivan@example.com").build();
        User saved = User.builder().id(1L).fullName("Ivan").email("ivan@example.com").build();
        UserResponseDto response = new UserResponseDto(1L, "Ivan", "ivan@example.com");

        when(userMapper.toEntity(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toResponseDto(saved)).thenReturn(response);

        UserResponseDto actual = userService.register(dto);

        assertEquals(1L, actual.getId());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(2L));
    }

    @Test
    void getByEmail_shouldFindIgnoringCase() {
        User user = User.builder().id(1L).fullName("Ivan").email("ivan@example.com").build();

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(new UserResponseDto(1L, "Ivan", "ivan@example.com"));

        UserResponseDto actual = userService.getByEmail("IVAN@EXAMPLE.COM");

        assertEquals("Ivan", actual.getFullName());
    }

    @Test
    void getByEmail_shouldThrowWhenNotFound() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByEmail("missing@example.com"));
    }

    @Test
    void update_shouldApplyFields() {
        UserRequestDto dto = new UserRequestDto("New Name", "new@example.com");
        User user = User.builder().id(3L).fullName("Old").email("old@example.com").build();
        UserResponseDto response = new UserResponseDto(3L, "New Name", "new@example.com");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(response);

        UserResponseDto actual = userService.update(3L, dto);

        assertEquals("New Name", user.getFullName());
        assertEquals("new@example.com", actual.getEmail());
    }

    @Test
    void delete_shouldThrowWhenNotExists() {
        when(userRepository.existsById(9L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(9L));
    }

    @Test
    void delete_shouldDeleteById() {
        when(userRepository.existsById(9L)).thenReturn(true);

        userService.delete(9L);

        verify(userRepository).deleteById(9L);
    }

    @Test
    void getAllUsers_shouldMapAll() {
        User user = User.builder().id(1L).fullName("Ivan").email("ivan@example.com").build();

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(new UserResponseDto(1L, "Ivan", "ivan@example.com"));

        List<UserResponseDto> actual = userService.getAllUsers();

        assertEquals(1, actual.size());
    }
}
