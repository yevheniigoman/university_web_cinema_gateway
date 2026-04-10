package com.iasaweb.cinemagateway.service;

import com.iasaweb.cinemagateway.dto.JwtDto;
import com.iasaweb.cinemagateway.exception.JwtValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.List;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class JwtValidationService {
    private final SecretKey signingKey;

    public JwtValidationService(@Value("${secret}") String secret) {
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public JwtDto validate(String jwt) throws JwtValidationException {
        JwtDto jwtDto;
        try {
            jwtDto = decode(jwt);
        } catch (JwtException e) {
            throw new JwtValidationException("Jwt token is not valid");
        }

        if (isExpired(jwtDto)) {
            throw new JwtValidationException("Jwt token is expired");
        }

        String username = jwtDto.username();
        if (username == null || username.isBlank()) {
            throw new JwtValidationException("Jwt token username is empty");
        }
        return jwtDto;
    }

    private JwtDto decode(String jwt) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);
        Date date = claims.getExpiration();
        ZonedDateTime expiration = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return new JwtDto(username, roles, expiration);
    }

    private boolean isExpired(JwtDto jwt) {
        return jwt.expiration().isBefore(ZonedDateTime.now());
    }
}