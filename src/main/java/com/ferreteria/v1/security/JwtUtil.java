package com.ferreteria.v1.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // Clave secreta — en producción va en variables de entorno, no en el código
    @Value("${jwt.secret:dGhpc0lzQVNlY3JldEtleUZvckZlcnJldGVyaWFQcm9ncmVzb2xDaGFyaXRvMjAyNg==}")
    private String secret;

    // 8 horas de validez del token
    private final long EXPIRATION_MS = 8 * 60 * 60 * 1000;

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Generar token ──────────────────────────────────
    public String generarToken(String email, String rol, Integer usuarioId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol)
                .claim("usuarioId", usuarioId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Extraer datos del token ────────────────────────
    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public String extraerRol(String token) {
        return extraerClaim(token, claims -> claims.get("rol", String.class));
    }

    public Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extraerTodosLosClaims(token);
        return resolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ── Validar token ──────────────────────────────────
    public boolean esTokenValido(String token, String email) {
        final String emailToken = extraerEmail(token);
        return (emailToken.equals(email) && !esTokenExpirado(token));
    }

    private boolean esTokenExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }
}