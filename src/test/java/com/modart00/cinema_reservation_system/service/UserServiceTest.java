package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.UserUpdatePasswordRequest;
import com.modart00.cinema_reservation_system.dto.request.UserUpdateUsernameRequest;
import com.modart00.cinema_reservation_system.dto.response.UserResponse;
import com.modart00.cinema_reservation_system.entity.Role;
import com.modart00.cinema_reservation_system.entity.User;
import com.modart00.cinema_reservation_system.exception.ConflictException;
import com.modart00.cinema_reservation_system.repository.ReservationRepository;
import com.modart00.cinema_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock MailService mailService;
    @Mock Authentication authentication;

    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, reservationRepository, passwordEncoder, mailService);
        user = new User();
        user.setId(1L);
        user.setUsername("ali");
        user.setEmail("ali@mail.com");
        user.setPassword("encoded");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDate.now());
    }

    @Test
    void getCurrentUser_shouldReturnDatabaseUser() {
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser(authentication);

        assertEquals("ali", response.getUsername());
        assertEquals("ROLE_USER", response.getRole());
    }

    @Test
    void updateUsername_whenAvailable_shouldSave() {
        UserUpdateUsernameRequest request = new UserUpdateUsernameRequest();
        request.setUsername("veli");
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("veli")).thenReturn(false);

        userService.updateUserName(request, authentication);

        assertEquals("veli", user.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void updateUsername_whenTaken_shouldThrow() {
        UserUpdateUsernameRequest request = new UserUpdateUsernameRequest();
        request.setUsername("veli");
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("veli")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUserName(request, authentication));
    }

    @Test
    void updatePassword_shouldEncodeAndInvalidateTokens() {
        user.setRefreshToken("refresh");
        UserUpdatePasswordRequest request = new UserUpdatePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncoded");

        userService.updatePassword(request, authentication);

        assertEquals("newEncoded", user.getPassword());
        assertNull(user.getRefreshToken());
        assertEquals(1, user.getTokenVersion());
    }
}
