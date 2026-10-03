package br.com.bibliotecaviva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.bibliotecaviva.dto.LoginRequest;
import br.com.bibliotecaviva.dto.LoginResponse;
import br.com.bibliotecaviva.exception.CredenciaisInvalidasException;
import br.com.bibliotecaviva.exception.UsuarioInativoException;
import br.com.bibliotecaviva.model.PerfilUsuario;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.UsuarioRepository;

class AuthServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private TokenService tokenService;
    private AuthService authService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenService = mock(TokenService.class);
        authService = new AuthService(usuarioRepository, passwordEncoder, tokenService);

        usuario = new Usuario();
        usuario.setNome("Administrador");
        usuario.setEmail("admin@biblioteca.com");
        usuario.setSenhaHash("hash-bcrypt");
        usuario.setPerfil(PerfilUsuario.ADMINISTRADOR);
        usuario.setAtivo(true);
    }

    @Test
    void deveAutenticarUsuarioAtivoComSenhaCorreta() {
        when(usuarioRepository.findByEmailIgnoreCase("admin@biblioteca.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hash-bcrypt")).thenReturn(true);
        when(tokenService.gerar(usuario)).thenReturn("jwt-valido");

        LoginResponse response = authService.login(
                new LoginRequest(" ADMIN@BIBLIOTECA.COM ", "password"));

        assertEquals("jwt-valido", response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals(PerfilUsuario.ADMINISTRADOR, response.perfil());
    }

    @Test
    void deveRejeitarEmailOuSenhaInvalidosComMensagemGenerica() {
        when(usuarioRepository.findByEmailIgnoreCase("inexistente@biblioteca.com"))
                .thenReturn(Optional.empty());

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(
                        new LoginRequest("inexistente@biblioteca.com", "senha-invalida")));

        assertEquals("E-mail ou senha inválidos", exception.getMessage());
    }

    @Test
    void deveBloquearLoginDeUsuarioInativo() {
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmailIgnoreCase("admin@biblioteca.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hash-bcrypt")).thenReturn(true);

        assertThrows(
                UsuarioInativoException.class,
                () -> authService.login(
                        new LoginRequest("admin@biblioteca.com", "password")));
    }

    @Test
    void deveRejeitarSenhaIncorretaDeUsuarioExistente() {
        when(usuarioRepository.findByEmailIgnoreCase("admin@biblioteca.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-incorreta", "hash-bcrypt")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login(new LoginRequest("admin@biblioteca.com", "senha-incorreta")));
    }
}
