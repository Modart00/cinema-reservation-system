package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.entity.VerificationToken;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public VerificationToken generateVerificationToken(){
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));
        return verificationToken;
    }

    public void sendMail(String to, String token) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        String subject = "Email verification";
        String text = "Merhaba,\nHesabınızı doğrulamak için:\n"
                + "http://localhost:8080/api/auth/verify?token=" + token;

        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        mailSender.send(mailMessage);
    }

}
