package com.bibliotech.auth.controller;

import com.bibliotech.auth.dto.AuthResponse;
import com.bibliotech.auth.dto.LoginRequest;
import com.bibliotech.auth.dto.RegisterRequest;
import com.bibliotech.auth.dto.UserDto;
import com.bibliotech.auth.entity.Role;
import com.bibliotech.auth.entity.UserStatus;
import com.bibliotech.auth.security.JwtAuthFilter;
import com.bibliotech.auth.security.JwtUtil;
import com.bibliotech.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_Returns201Created() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123", "STUDENT");
        UserDto userDto = new UserDto(1L, "Jane Doe", "jane@example.com", "STUDENT", "ACTIVE", null);
        AuthResponse authResponse = new AuthResponse("token-123", "Bearer", userDto);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.user.email").value("jane@example.com"));
    }

    @Test
    void login_Returns200Ok() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        UserDto userDto = new UserDto(1L, "Jane Doe", "jane@example.com", "STUDENT", "ACTIVE", null);
        AuthResponse authResponse = new AuthResponse("token-123", "Bearer", userDto);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"));
    }
}
