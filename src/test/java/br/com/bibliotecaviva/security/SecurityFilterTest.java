package br.com.bibliotecaviva.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.bibliotecaviva.model.PerfilUsuario;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.UsuarioRepository;
import br.com.bibliotecaviva.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class SecurityFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCarregarPerfilDoBancoParaTokenValido() throws Exception {
        TokenService tokenService = mock(TokenService.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        Usuario usuario = usuario(PerfilUsuario.ATENDENTE, true);

        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(tokenService.validarEObterId("token-valido"))
                .thenReturn(Optional.of(42L));
        when(usuarioRepository.findById(42L))
                .thenReturn(Optional.of(usuario));

        new SecurityFilter(tokenService, usuarioRepository).doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(usuario, authentication.getPrincipal());
        assertEquals("ROLE_ATENDENTE", authentication.getAuthorities().iterator().next().getAuthority());
        verify(chain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarUsuarioInativo() throws Exception {
        TokenService tokenService = mock(TokenService.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(tokenService.validarEObterId("token-valido"))
                .thenReturn(Optional.of(42L));
        when(usuarioRepository.findById(42L))
                .thenReturn(Optional.of(usuario(PerfilUsuario.ATENDENTE, false)));

        new SecurityFilter(tokenService, usuarioRepository).doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    private Usuario usuario(PerfilUsuario perfil, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setNome("Atendente");
        usuario.setEmail("atendente@biblioteca.com");
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);
        return usuario;
    }
}
