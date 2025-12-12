package com.smarthome.webapp.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.smarthome.webapp.auth.repository.AuthAccountDocument;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtkey;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        SecretKey key = Keys.hmacShaKeyFor(jwtkey.getBytes(StandardCharsets.UTF_8));
        final Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claimsResolver.apply(claims);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(jwtkey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().claims(claims).subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)).signWith(key).compact();
    }

    private String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public String generateToken(AuthAccountDocument userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public HashMap<String,Object> generateRefreshToken(AuthAccountDocument userDetails) {
        HashMap<String,Object> tokenData = new HashMap<String,Object>();

        String token = createRefreshToken();
        Date exp = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30);

        tokenData.put("token", token);
        tokenData.put("exp", exp);

        return tokenData;
    }

    public Boolean validateRefreshToken(String refreshToken, AuthAccountDocument user) {
        Date now = new Date(System.currentTimeMillis());
        if (refreshToken.equals(user.getRefreshToken()) && !(user.getRefreshTokenExp().after(now))) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean validateToken(String token, AuthAccountDocument userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
