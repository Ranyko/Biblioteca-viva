package br.com.bibliotecaviva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.bibliotecaviva.model.PerfilUsuario;
import br.com.bibliotecaviva.model.Usuario;

class TokenServiceTest {

    private static final String SECRET =
            "segredo-de-teste-com-tamanho-suficiente-para-o-hmac256";

    @Test
    void deveGerarEValidarTokenDoUsuario() {
        TokenService tokenService = new TokenService(SECRET, "biblioteca-viva-teste", 2);
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 42L);
        usuario.setNome("Maria");
        usuario.setEmail("maria@biblioteca.com");
        usuario.setPerfil(PerfilUsuario.LEITOR);
        usuario.setAtivo(true);

        String token = tokenService.gerar(usuario);

        assertEquals(42L, tokenService.validarEObterId(token).orElseThrow());
    }

    @Test
    void deveRejeitarTokenAssinadoComOutroSegredo() {
        TokenService emissor = new TokenService(SECRET, "biblioteca-viva-teste", 2);
        TokenService validador = new TokenService(
                "outro-segredo-de-teste-com-tamanho-suficiente-hmac256",
                "biblioteca-viva-teste",
                2);
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 42L);
        usuario.setEmail("maria@biblioteca.com");
        usuario.setPerfil(PerfilUsuario.LEITOR);

        assertTrue(validador.validarEObterId(emissor.gerar(usuario)).isEmpty());
    }

    @Test
    void deveRejeitarTokenExpirado() {
        TokenService service = new TokenService(SECRET, "biblioteca-viva-teste", 2);
        String token = JWT.create().withIssuer("biblioteca-viva-teste").withSubject("42")
                .withExpiresAt(Instant.now().minusSeconds(60)).sign(Algorithm.HMAC256(SECRET));
        assertTrue(service.validarEObterId(token).isEmpty());
    }

    @Test
    void deveRejeitarSubjectAntigoBaseadoEmEmail() {
        TokenService service = new TokenService(SECRET, "biblioteca-viva-teste", 2);
        String token = JWT.create().withIssuer("biblioteca-viva-teste")
                .withSubject("admin@biblioteca.com")
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256(SECRET));
        assertTrue(service.validarEObterId(token).isEmpty());
    }
}
