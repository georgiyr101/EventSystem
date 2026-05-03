package com.example.eventsystem.security;

import com.example.eventsystem.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthHandlers {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                -> writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                "Authentication is required");
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
                -> writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                "You do not have permission to access this resource");
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, int status, String error,
                            String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                status,
                error,
                message,
                request.getRequestURI(),
                null
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
