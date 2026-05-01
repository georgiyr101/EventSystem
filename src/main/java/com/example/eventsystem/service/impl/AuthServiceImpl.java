package com.example.eventsystem.service.impl;

import com.example.eventsystem.exception.ConflictException;
import com.example.eventsystem.exception.ResourceNotFoundException;
import com.example.eventsystem.exception.ValidationException;
import com.example.eventsystem.model.dto.auth.AuthResponseDto;
import com.example.eventsystem.model.dto.auth.LoginRequestDto;
import com.example.eventsystem.model.dto.auth.ProfileUpdateRequestDto;
import com.example.eventsystem.model.dto.auth.RegisterRequestDto;
import com.example.eventsystem.model.entity.Organizer;
import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.model.enums.AppRole;
import com.example.eventsystem.repository.OrganizerRepository;
import com.example.eventsystem.repository.UserRepository;
import com.example.eventsystem.security.JwtService;
import com.example.eventsystem.security.UserPrincipal;
import com.example.eventsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (dto.getRole() == AppRole.ADMIN) {
            throw new ValidationException("ADMIN role cannot be registered via public endpoint");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("User with this email already exists");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        if (dto.getRole() == AppRole.ORGANIZER) {
            Organizer organizer = Organizer.builder()
                    .name(dto.getFullName())
                    .contactInfo(dto.getEmail())
                    .build();
            Organizer savedOrganizer = organizerRepository.save(organizer);
            user.setOrganizerProfile(savedOrganizer);
        }

        User saved = userRepository.save(user);
        return toAuthResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ValidationException("Account has no password set; register a new account or reset credentials");
        }

        return toAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto me() {
        UserPrincipal principal = currentUser();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));
        return toAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDto updateProfile(ProfileUpdateRequestDto dto) {
        UserPrincipal principal = currentUser();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        userRepository.findByEmail(dto.getEmail()).ifPresent(other -> {
            if (!other.getId().equals(user.getId())) {
                throw new ConflictException("User with this email already exists");
            }
        });

        if (StringUtils.hasText(dto.getNewPassword())) {
            if (!StringUtils.hasText(dto.getCurrentPassword())) {
                throw new ValidationException("Current password is required to set a new password");
            }
            if (user.getPasswordHash() == null || !passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
                throw new ValidationException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());

        User saved = userRepository.save(user);
        return toAuthResponse(saved);
    }

    private AuthResponseDto toAuthResponse(User user) {
        Long organizerId = user.getOrganizerProfile() != null ? user.getOrganizerProfile().getId() : null;
        String token = jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole(), organizerId);
        return new AuthResponseDto(
                "Bearer",
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                organizerId
        );
    }

    private UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new ValidationException("Not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }
}
