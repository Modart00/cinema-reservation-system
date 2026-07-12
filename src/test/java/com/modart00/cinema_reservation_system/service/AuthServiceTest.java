package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.LoginRequest;
import com.modart00.cinema_reservation_system.dto.request.RegisterRequest;
import com.modart00.cinema_reservation_system.dto.response.LoginResponse;
import com.modart00.cinema_reservation_system.entity.Role;
import com.modart00.cinema_reservation_system.entity.User;
import com.modart00.cinema_reservation_system.entity.VerificationToken;
import com.modart00.cinema_reservation_system.exception.AccountNotVerifiedException;
import com.modart00.cinema_reservation_system.repository.UserRepository;
import com.modart00.cinema_reservation_system.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock JwtService jwtService;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock MailService mailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtService, userRepository, passwordEncoder, mailService);
    }

    @Test
    void register_shouldCreateDisabledUserAndSendVerificationMail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ali");
        request.setEmail("ali@mail.com");
        request.setPassword("password123");
        VerificationToken token = new VerificationToken();
        token.setToken("verification-token");
        token.setExpiryDate(new Date(System.currentTimeMillis() + 60_000));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(mailService.generateVerificationToken()).thenReturn(token);

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.ROLE_USER
                        && !user.isEnabled()
                        && "encoded".equals(user.getPassword())
        ));
        verify(mailService).sendMail("ali@mail.com", "verification-token");
    }

    @Test
    void login_whenCredentialsValid_shouldReturnTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ali@mail.com");
        request.setPassword("password123");
        User user = new User();
        user.setId(1L);
        user.setEmail(request.getEmail());
        user.setPassword("encoded");
        user.setEnabled(true);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), "encoded")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");

        LoginResponse response = authService.login(request);

        assertEquals("access-token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository).save(user);
    }

    @Test
    void login_whenAccountDisabled_shouldThrow() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ali@mail.com");
        request.setPassword("password123");
        User user = new User();
        user.setPassword("encoded");
        user.setEnabled(false);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), "encoded")).thenReturn(true);

        assertThrows(AccountNotVerifiedException.class, () -> authService.login(request));
    }
}
