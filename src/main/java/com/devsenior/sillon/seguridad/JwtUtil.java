package com.devsenior.sillon.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final Clock clock;
    private final long duracionMilisegundos = 1000L * 60 * 60 * 24; // 24 horas

    public JwtUtil(@Value("${jwt.secreto}") String secreto, Clock clock) {
        this.key = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.clock = clock;
    }

    public String generarToken(String email, Long salonId, String rol) {
        Instant ahora = clock.instant();
        Instant expiracion = ahora.plusMillis(duracionMilisegundos);

        return Jwts.builder()
                .subject(email)
                .claim("salonId", salonId)
                .claim("rol", rol)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(key)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    public Long extraerSalonId(String token) {
        Object salonIdObj = extraerClaims(token).get("salonId");
        if (salonIdObj instanceof Number num) {
            return num.longValue();
        }
        return null;
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public boolean esTokenValido(String token) {
        try {
            Claims claims = extraerClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(Date.from(clock.instant()));
        } catch (Exception e) {
            return false;
        }
    }
}