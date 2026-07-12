package com.modart00.cinema_reservation_system.security;

import com.modart00.cinema_reservation_system.entity.User;
import com.modart00.cinema_reservation_system.entity.VerificationToken;
import com.modart00.cinema_reservation_system.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityAndMailTest {

    @Test
    void jwtService_shouldGenerateAndValidateToken() {
        String secret = Base64.getEncoder().encodeToString(
                "cinema-test-secret-key-with-32-bytes-minimum".getBytes()
        );
        JwtService jwtService = new JwtService(secret);
        User user = new User();
        user.setEmail("ali@mail.com");
        user.setTokenVersion(2);

        String token = jwtService.generateToken(user);

        assertEquals("ali@mail.com", jwtService.extractEmail(token));
        assertTrue(jwtService.validateToken(token, user));
        user.setTokenVersion(3);
        assertFalse(jwtService.validateToken(token, user));
    }

    @Test
    void mailService_shouldGenerateTokenAndSendMail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MailService mailService = new MailService(mailSender);

        VerificationToken token = mailService.generateVerificationToken();
        mailService.sendMail("ali@mail.com", token.getToken());

        assertNotNull(token.getToken());
        assertNotNull(token.getExpiryDate());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
