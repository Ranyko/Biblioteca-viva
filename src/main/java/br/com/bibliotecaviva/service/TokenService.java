package br.com.bibliotecaviva.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.com.bibliotecaviva.model.Usuario;

@Service
public class TokenService {

    private final Algorithm algorithm;
    private final String issuer;
    private final long expirationHours;

    public TokenService(
            @Value("${api.security.token.secret}") String secret,
            @Value("${api.security.token.issuer}") String issuer,
            @Value("${api.security.token.expiration-hours}") long expirationHours) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expirationHours = expirationHours;
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(usuario.getId().toString())
                .withClaim("usuarioId", usuario.getId())
                .withClaim("perfil", usuario.getPerfil().valor())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plus(expirationHours, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    public Optional<Long> validarEObterId(String token) {
        try {
            String subject = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
            long id = Long.parseLong(subject);
            return id > 0 ? Optional.of(id) : Optional.empty();
        } catch (JWTVerificationException | NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
