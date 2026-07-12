package com.modart00.cinema_reservation_system.security;

import com.modart00.cinema_reservation_system.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String secretKey;

    public JwtService(@Value("${JWT_SECRET}") String secretKey) {
        this.secretKey = secretKey;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user){
        SecretKey secretKey = getSigningKey();

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(secretKey)
                .compact();
    }

    public Claims extractAllClaims(String token){
        SecretKey secretKey = getSigningKey();

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token){
        String email = extractAllClaims(token).getSubject();
        return email;
    }

    public Date extractExpiration(String token){
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration;
    }

    public boolean isTokenExpired(String token){
        Date currentTime = new Date(System.currentTimeMillis());
        Date expiration = extractExpiration(token);

        if (currentTime.after(expiration)){
            return true;
        }else return false;
    }

    public boolean validateToken(String token,User user){
        String email = extractEmail(token);
        if (isTokenExpired(token) || !email.equals(user.getEmail())){
            return false;
        } else return true;
    }


}
