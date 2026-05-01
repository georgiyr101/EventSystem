package com.example.eventsystem.controller;

import com.example.eventsystem.model.dto.auth.AuthResponseDto;
import com.example.eventsystem.model.dto.auth.LoginRequestDto;
import com.example.eventsystem.model.dto.auth.ProfileUpdateRequestDto;
import com.example.eventsystem.model.dto.auth.RegisterRequestDto;
import com.example.eventsystem.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "JWT authentication")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register")
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @Operation(summary = "Login")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Current user profile + fresh token")
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> me() {
        return ResponseEntity.ok(authService.me());
    }

    @Operation(summary = "Update current user profile (optional password change)")
    @PutMapping("/profile")
    public ResponseEntity<AuthResponseDto> updateProfile(@Valid @RequestBody ProfileUpdateRequestDto dto) {
        return ResponseEntity.ok(authService.updateProfile(dto));
    }
}
