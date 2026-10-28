package org.esfe.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.esfe.models.Usuario;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // Clave secreta (debe tener al menos 256 bits/32 caracteres en Base64)
    private static final String SECRET_KEY = "NDAyODgwODFiOTM1YjM4ZjAxYjkzNWIzOGYwMTAwMDA=";

    // Sobrecarga conveniente para pasar la entidad Usuario directamente
    public String generarToken(Usuario usuario) {
        String nombreRol = (usuario.getRol() != null) ? usuario.getRol().getNombre() : "USER";
        return generarToken(usuario.getEmail(), nombreRol);
    }

    public String generarToken(String email, String rol) {
        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 24 horas
                .signWith(obtenerLlaveFirma(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String obtenerEmailDelToken(String token) {
        return obtenerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, String email) {
        final String username = obtenerEmailDelToken(token);
        return (username.equals(email) && !esTokenExpirado(token));
    }

    private boolean esTokenExpirado(String token) {
        return obtenerClaims(token).getExpiration().before(new Date());
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(obtenerLlaveFirma())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key obtenerLlaveFirma() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}