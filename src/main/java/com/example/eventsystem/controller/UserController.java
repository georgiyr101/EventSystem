package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.UserRequestDto;
import com.example.eventsystem.model.dto.UserResponseDto;
import com.example.eventsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Регистрация нового пользователя в системе.
     * POST /api/v1/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto requestDto) {
        return new ResponseEntity<>(userService.register(requestDto), HttpStatus.CREATED);
    }

    /**
     * Получение профиля пользователя по ID.
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /**
     * Поиск пользователя по адресу электронной почты.
     * GET /api/v1/users/find?email=test@example.com
     */
    @GetMapping("/find")
    public ResponseEntity<UserResponseDto> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    /**
     * Обновление данных пользователя.
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable Long id, @RequestBody UserRequestDto requestDto) {
        return ResponseEntity.ok(userService.update(id, requestDto));
    }

    /**
     * Удаление аккаунта пользователя.
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
