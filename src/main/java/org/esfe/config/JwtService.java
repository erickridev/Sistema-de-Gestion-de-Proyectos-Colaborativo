
package org.esfe.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.esfe.models.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final Key llaveFirma;
    private final long expiracion;

    // CONFIGURACION DEL SERVICIO JWT
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expiracion
    ) {

        // DECODIFICAR CLAVE BASE64
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        // VALIDAR LONGITUD DE LA CLAVE
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "La clave JWT debe tener al menos 256 bits"
            );
        }

        // VALIDAR TIEMPO DE EXPIRACION
        if (expiracion <= 0) {
            throw new IllegalArgumentException(
                    "La expiracion JWT debe ser positiva"
            );
        }

        this.llaveFirma = Keys.hmacShaKeyFor(keyBytes);
        this.expiracion = expiracion;
    }

    // GENERAR TOKEN DESDE USUARIO
    public String generarToken(Usuario usuario) {

        if (usuario == null ||
                usuario.getEmail() == null ||
                usuario.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "El usuario debe tener un correo valido"
            );
        }

        if (usuario.getRol() == null ||
                usuario.getRol().getNombre() == null ||
                usuario.getRol().getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El usuario debe tener un rol valido"
            );
        }

        return generarToken(
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );
    }

    // GENERAR TOKEN JWT
    public String generarToken(String email, String rol) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo electronico es obligatorio"
            );
        }

        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        long tiempoActual = System.currentTimeMillis();

        Date fechaActual = new Date(tiempoActual);

        Date fechaExpiracion = new Date(
                Math.addExact(tiempoActual, expiracion)
        );

        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol)
                .setIssuedAt(fechaActual)
                .setExpiration(fechaExpiracion)
                .signWith(
                        llaveFirma,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    // OBTENER EMAIL DEL TOKEN
    public String obtenerEmailDelToken(String token) {
        return obtenerClaims(token).getSubject();
    }

    // VALIDAR TOKEN JWT
    public boolean esTokenValido(
            String token,
            String email
    ) {

        if (email == null || email.isBlank()) {
            return false;
        }

        Claims claims = obtenerClaims(token);

        return email.equals(claims.getSubject())
                && claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
    }

    // OBTENER CLAIMS DEL TOKEN
    private Claims obtenerClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token JWT es obligatorio"
            );
        }

        return Jwts.parserBuilder()
                .setSigningKey(llaveFirma)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
