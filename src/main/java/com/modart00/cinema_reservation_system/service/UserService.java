package com.modart00.cinema_reservation_system.service;

import com.modart00.cinema_reservation_system.dto.request.UserUpdateEmailRequest;
import com.modart00.cinema_reservation_system.dto.request.UserUpdatePasswordRequest;
import com.modart00.cinema_reservation_system.dto.request.UserUpdateUsernameRequest;
import com.modart00.cinema_reservation_system.dto.response.UpdateEmailResponse;
import com.modart00.cinema_reservation_system.dto.response.UserResponse;
import com.modart00.cinema_reservation_system.entity.*;
import com.modart00.cinema_reservation_system.exception.ConflictException;
import com.modart00.cinema_reservation_system.exception.InvalidRequestException;
import com.modart00.cinema_reservation_system.exception.ResourceNotFoundException;
import com.modart00.cinema_reservation_system.repository.ReservationRepository;
import com.modart00.cinema_reservation_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;


    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
//admin method
    public UserResponse getUserById(Long id) {
        log.info("Admin attempting to get user, user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", id);
                    return new ResourceNotFoundException("Kullanıcı bulunamadı");
                });

        log.info("Admin got user, user ID: {}", id);
        return toResponse(user);
    }

    public UserResponse getCurrentUser(Authentication authentication){
        User user = getAuthenticatedUser(authentication);
        log.info("User {} attempt to get Profile", user.getUsername());
        log.info("User {} got Profile",user.getUsername());
        return toResponse(user);
    }
    //admin method
    public Page<UserResponse> getAllUsers(Pageable pageable){
        log.info("Admin attempt to get All Users");
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void updateUserName(UserUpdateUsernameRequest request, Authentication authentication){
        User user = getAuthenticatedUser(authentication);
        log.info("User {} attempt to change username", user.getUsername());
        String newUsername = request.getUsername().trim();
        if (!user.getUsername().equals(newUsername)
                && userRepository.existsByUsername(newUsername)) {
            log.warn("User {} tried to change username to an existing username",user.getUsername());
            throw new ConflictException("Bu kullanıcı adı kullanımda");
        }
        user.setUsername(newUsername);
        log.info("User {} changed username",user.getUsername());
        userRepository.save(user);
    }

    @Transactional
    public UpdateEmailResponse updateEmail(
            UserUpdateEmailRequest request,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        log.info("User {} attempting to change email", user.getUsername());

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {
            log.warn("Incorrect password during email change for user {}", user.getId());
            throw new InvalidRequestException("Mevcut şifre hatalı");
        }

        String newEmail = request.getNewEmail().trim().toLowerCase(Locale.ROOT);

        if (user.getEmail().equalsIgnoreCase(newEmail)) {
            throw new InvalidRequestException("Yeni e-posta mevcut e-posta ile aynı");
        }

        if (userRepository.existsByEmail(newEmail)) {
            log.warn("Email change attempted with existing email: {}", newEmail);
            throw new ConflictException("Bu e-posta zaten kullanımda");
        }

        VerificationToken generatedToken = mailService.generateVerificationToken();
        VerificationToken verificationToken = user.getToken();

        if (verificationToken == null) {
            verificationToken = generatedToken;
        } else {
            verificationToken.setToken(generatedToken.getToken());
            verificationToken.setExpiryDate(generatedToken.getExpiryDate());
        }
        verificationToken.setUser(user);

        user.setEmail(newEmail);
        user.setEnabled(false);
        user.setToken(verificationToken);

        // Mevcut oturumu geçersiz kıl
        user.setRefreshToken(null);
        user.setRefreshTokenExpiryDate(null);
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        mailService.sendMail(newEmail, verificationToken.getToken());

        log.info("Email changed for user ID {}. Verification required.", user.getId());
        return new UpdateEmailResponse("Email adresiniz güncellendi. Yeni email adresinize gönderilen bağlantıdan hesabınızı doğrulayın ve tekrar giriş yapın");
    }

    @Transactional
    public void updatePassword(UserUpdatePasswordRequest request, Authentication authentication){
        User user = getAuthenticatedUser(authentication);
        log.info("User {} attempt to change password", user.getUsername());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
            log.warn("Password change attempt failed: passwords doesn't match ");
            throw new InvalidRequestException("Girdiğiniz şifre mevcut şifrenizle uyuşmuyor, lütfen tekrar deneyiniz");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRefreshToken(null);
        user.setRefreshTokenExpiryDate(null);
        user.setTokenVersion(user.getTokenVersion() + 1);
        log.info("User {} changed password",user.getUsername());
        userRepository.save(user);
    }

    @Transactional
    public void deleteCurrentUser(Authentication authentication){
        User user = getAuthenticatedUser(authentication);
        log.info("User {} attempt to delete the account", user.getUsername());
        ensureUserCanBeDeleted(user);
        userRepository.delete(user);
        log.info("User {} deleted the account",user.getUsername());
    }
    //admin method
    @Transactional
    public void deleteAnyUserById(Long id){
        log.info("Admin attempt to delete account, account ID: {}",id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Account with ID: {} not found", id);
                    return new ResourceNotFoundException("Kullanıcı bulunamadı");
                });
        ensureUserCanBeDeleted(user);
        userRepository.delete(user);
        log.info("Admin deleted account,account ID: {}",id);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User principal)) {
            throw new InvalidRequestException("Geçerli bir kullanıcı oturumu bulunamadı");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
    }

    private void ensureUserCanBeDeleted(User user) {
        if (reservationRepository.existsByUserId(user.getId())) {
            throw new ConflictException("Rezervasyonu bulunan kullanıcı hesabı silinemez");
        }
    }

}
