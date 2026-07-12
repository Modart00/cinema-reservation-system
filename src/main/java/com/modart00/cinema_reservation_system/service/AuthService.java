package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.LoginRequest;
import com.modart00.cinema_reservation_system.dto.request.RegisterRequest;
import com.modart00.cinema_reservation_system.dto.response.LoginResponse;
import com.modart00.cinema_reservation_system.dto.response.RegisterResponse;
import com.modart00.cinema_reservation_system.entity.Role;
import com.modart00.cinema_reservation_system.entity.User;
import com.modart00.cinema_reservation_system.entity.VerificationToken;
import com.modart00.cinema_reservation_system.exception.AccountNotVerifiedException;
import com.modart00.cinema_reservation_system.repository.UserRepository;
import com.modart00.cinema_reservation_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public RegisterResponse register(RegisterRequest request){
        log.info("register attempt for email {}",request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())){
            log.warn("Duplicate email registration attempt: {}",request.getEmail());
            throw new RuntimeException("Bu email sistemde kayıtlı");
        }

        if (userRepository.existsByUserName(request.getUsername())){
            log.warn("Duplicate username registration attempt: {}" ,request.getUsername());
            throw new RuntimeException("Bu kullanıcı adı sisteme kayıtlı");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDate.now());
        user.setEnabled(false);
        VerificationToken token = mailService.generateVerificationToken();
        token.setUser(user);
        user.setToken(token);
        mailService.sendMail(request.getEmail(), token.getToken());
        userRepository.save(user);
        log.info("User {} registered successfully",user.getUsername());
        RegisterResponse response = new RegisterResponse();
        return response;
    }

    public LoginResponse login(LoginRequest request){
        log.info("login attempt for {}",request.getEmail());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
            log.warn("User with email {} not found!", request.getEmail());
           return new RuntimeException("Kullanıcı bulunamadı");
        });
        String attemptedPassword = passwordEncoder.encode(request.getPassword());
        String attemptedEmail = request.getEmail();

        if (!user.getEmail().equals(attemptedEmail) || !user.getPassword().equals(attemptedPassword)){
            log.warn("Invalid credentials for {}",request.getEmail());
            throw new RuntimeException("Kullanıcı adı veya şifre hatalı, lütfen tekrar deneyin");
        }
        if (user.isEnabled() == false) {
            log.warn("Account not verified for {}",request.getEmail());
            throw new AccountNotVerifiedException("Hesabınız doğrulanmadı, lütfen hesabınızı doğrulayınız.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();

        return new LoginResponse(accessToken,refreshToken);

    }

    public LoginResponse refreshToken(String refreshToken){
        User user = userRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new RuntimeException("Refresh Token bulunamadı."));
        if (user.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())){throw new RuntimeException("Refresh Token süresi doldu");}
        String accessToken = jwtService.generateToken(user);
        return new LoginResponse(accessToken,refreshToken);
    }

    public void verifyAccount(String token){
        User user = userRepository.findByToken_Token(token).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        if (user.getToken().getToken().equals(token)){
            user.setEnabled(true);
            userRepository.save(user);
        }

    }


}
