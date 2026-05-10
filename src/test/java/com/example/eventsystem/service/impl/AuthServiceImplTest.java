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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizerRepository organizerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldRejectAdminRole() {
        RegisterRequestDto dto = regDto();
        dto.setRole(AppRole.ADMIN);

        assertThrows(ValidationException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequestDto dto = regDto();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(User.builder().id(9L).build()));

        assertThrows(ConflictException.class, () -> authService.register(dto));
    }

    @Test
    void register_user_shouldSaveAndReturnToken() {
        RegisterRequestDto dto = regDto();
        dto.setRole(AppRole.USER);
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");
        User saved = User.builder().id(1L).email(dto.getEmail()).fullName(dto.getFullName())
                .passwordHash("enc").role(AppRole.USER).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.createAccessToken(eq(1L), eq(dto.getEmail()), eq(AppRole.USER), eq(null)))
                .thenReturn("jwt");

        AuthResponseDto response = authService.register(dto);

        assertEquals("jwt", response.getAccessToken());
        verify(organizerRepository, never()).save(any());
    }

    @Test
    void register_organizer_shouldCreateProfile() {
        RegisterRequestDto dto = regDto();
        dto.setRole(AppRole.ORGANIZER);
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");
        Organizer org = Organizer.builder().id(50L).name(dto.getFullName()).build();
        when(organizerRepository.save(any(Organizer.class))).thenReturn(org);
        User saved = User.builder().id(2L).email(dto.getEmail()).fullName(dto.getFullName())
                .passwordHash("enc").role(AppRole.ORGANIZER).organizerProfile(org).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.createAccessToken(eq(2L), eq(dto.getEmail()), eq(AppRole.ORGANIZER), eq(50L)))
                .thenReturn("jwt");

        AuthResponseDto response = authService.register(dto);

        assertEquals(50L, response.getOrganizerId());
        verify(organizerRepository).save(any(Organizer.class));
    }

    @Test
    void login_shouldReturnTokenWhenPasswordSet() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("u@e.com");
        dto.setPassword("secret");
        User userEntity = User.builder().id(3L).email("u@e.com").passwordHash("hash")
                .fullName("U").role(AppRole.USER).build();
        UserPrincipal principal = new UserPrincipal(userEntity);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(3L)).thenReturn(Optional.of(userEntity));
        when(jwtService.createAccessToken(eq(3L), eq("u@e.com"), eq(AppRole.USER), eq(null))).thenReturn("t");

        AuthResponseDto response = authService.login(dto);

        assertEquals("t", response.getAccessToken());
    }

    @Test
    void login_shouldThrowWhenUserMissingAfterAuth() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("u@e.com");
        dto.setPassword("secret");
        User userEntity = User.builder().id(3L).email("u@e.com").role(AppRole.USER).build();
        UserPrincipal principal = new UserPrincipal(userEntity);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrowWhenPasswordHashNull() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("u@e.com");
        dto.setPassword("secret");
        User userEntity = User.builder().id(3L).email("u@e.com").passwordHash(null)
                .role(AppRole.USER).build();
        UserPrincipal principal = new UserPrincipal(userEntity);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(3L)).thenReturn(Optional.of(userEntity));

        assertThrows(ValidationException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrowWhenPasswordHashMissing() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("u@e.com");
        dto.setPassword("secret");
        User userEntity = User.builder().id(3L).email("u@e.com").passwordHash("  ")
                .role(AppRole.USER).build();
        UserPrincipal principal = new UserPrincipal(userEntity);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(3L)).thenReturn(Optional.of(userEntity));

        assertThrows(ValidationException.class, () -> authService.login(dto));
    }

    @Test
    void me_shouldReturnCurrentUser() {
        User userEntity = User.builder().id(4L).email("m@e.com").fullName("M")
                .passwordHash("h").role(AppRole.USER).build();
        login(userEntity);
        when(userRepository.findById(4L)).thenReturn(Optional.of(userEntity));
        when(jwtService.createAccessToken(eq(4L), eq("m@e.com"), eq(AppRole.USER), eq(null))).thenReturn("tok");

        AuthResponseDto response = authService.me();

        assertEquals("tok", response.getAccessToken());
    }

    @Test
    void me_shouldThrowWhenNotAuthenticated() {
        assertThrows(ValidationException.class, () -> authService.me());
    }

    @Test
    void me_shouldThrowWhenUserMissing() {
        User userEntity = User.builder().id(4L).email("m@e.com").role(AppRole.USER).build();
        login(userEntity);
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.me());
    }

    @Test
    void updateProfile_shouldUpdateWithoutPasswordChange() {
        User userEntity = User.builder().id(5L).email("old@e.com").fullName("Old")
                .passwordHash("hash").role(AppRole.USER).build();
        login(userEntity);
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("New Name", "new@e.com", null, null);
        when(userRepository.findById(5L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("new@e.com")).thenReturn(Optional.empty());
        User saved = User.builder().id(5L).email("new@e.com").fullName("New Name")
                .passwordHash("hash").role(AppRole.USER).build();
        when(userRepository.save(userEntity)).thenReturn(saved);
        when(jwtService.createAccessToken(eq(5L), eq("new@e.com"), eq(AppRole.USER), eq(null))).thenReturn("j");

        AuthResponseDto response = authService.updateProfile(dto);

        assertEquals("new@e.com", response.getEmail());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateProfile_shouldThrowWhenEmailTakenByOther() {
        User userEntity = User.builder().id(5L).email("old@e.com").fullName("Old")
                .passwordHash("hash").role(AppRole.USER).build();
        login(userEntity);
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("N", "taken@e.com", null, null);
        when(userRepository.findById(5L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("taken@e.com"))
                .thenReturn(Optional.of(User.builder().id(99L).build()));

        assertThrows(ConflictException.class, () -> authService.updateProfile(dto));
    }

    @Test
    void updateProfile_shouldAllowKeepingSameEmail() {
        User userEntity = User.builder().id(5L).email("same@e.com").fullName("Old")
                .passwordHash("hash").role(AppRole.USER).build();
        login(userEntity);
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("New", "same@e.com", null, null);
        when(userRepository.findById(5L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("same@e.com")).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(jwtService.createAccessToken(any(), any(), any(), ArgumentMatchers.any())).thenReturn("x");

        authService.updateProfile(dto);
    }

    @Test
    void updateProfile_shouldThrowWhenPrincipalIsNotUserPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null, List.of()));

        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("A", "a@b.com", null, null);

        assertThrows(ValidationException.class, () -> authService.updateProfile(dto));
    }

    @Test
    void updateProfile_shouldThrowWhenPasswordHashNullAndChangingPassword() {
        User userEntity = User.builder().id(5L).email("a@b.com").fullName("A")
                .passwordHash(null).role(AppRole.USER).build();
        login(userEntity);
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("A", "a@b.com", "any", "newpass12345");
        when(userRepository.findById(5L)).thenReturn(Optional.of(userEntity));

        assertThrows(ValidationException.class, () -> authService.updateProfile(dto));
    }

    @Test
    void register_shouldIncludeOrganizerIdInResponseWhenProfilePresent() {
        RegisterRequestDto dto = regDto();
        dto.setRole(AppRole.USER);
        Organizer org = Organizer.builder().id(77L).name("O").build();
        User saved = User.builder().id(8L).email(dto.getEmail()).fullName(dto.getFullName())
                .passwordHash("enc").role(AppRole.USER).organizerProfile(org).build();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("enc");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.createAccessToken(eq(8L), any(), eq(AppRole.USER), eq(77L))).thenReturn("j");

        AuthResponseDto response = authService.register(dto);

        assertEquals(77L, response.getOrganizerId());
    }

    @Test
    void updateProfile_shouldRequireCurrentPasswordForNewPassword() {
        User userEntity = User.builder().id(5L).email("a@b.com").fullName("A")
                .passwordHash("hash").role(AppRole.USER).build();
        login(userEntity);
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("A", "a@b.com", null, "newpass123");
        when(userRepository.findById(5L)).thenReturn(Optional.of(userEntity));

        assertThrows(ValidationException.class, () -> authService.updateProfile(dto));
    }

    @Test
    void updateProfile_shouldRejectWrongCurrentPassword() {
        User userEntity = User.builder().id(5L).email("a@b.com").fullName("A")
                .passwordHash("hash").role(AppRole.USER).build();
        login(userEntity);
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("A", "a@b.com", "wrong", "newpass12345");
        when(userRepository.findById(5L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(ValidationException.class, () -> authService.updateProfile(dto));
    }

    @Test
    void updateProfile_shouldUpdatePasswordWhenCurrentMatches() {
        User userEntity = User.builder().id(5L).email("a@b.com").fullName("A")
                .passwordHash("hash").role(AppRole.USER).build();
        login(userEntity);
        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto("A", "a@b.com", "oldpass", "newpass12345");
        when(userRepository.findById(5L)).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("oldpass", "hash")).thenReturn(true);
        when(passwordEncoder.encode("newpass12345")).thenReturn("newhash");
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(jwtService.createAccessToken(any(), any(), any(), ArgumentMatchers.any())).thenReturn("z");

        authService.updateProfile(dto);

        verify(passwordEncoder).encode("newpass12345");
    }

    private static RegisterRequestDto regDto() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("ivan@example.com");
        dto.setFullName("Ivan");
        dto.setPassword("password123");
        dto.setRole(AppRole.USER);
        return dto;
    }

    private static void login(User userEntity) {
        UserPrincipal principal = new UserPrincipal(userEntity);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
