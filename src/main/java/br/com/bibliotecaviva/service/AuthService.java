package br.com.bibliotecaviva.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.bibliotecaviva.dto.LoginRequest;
import br.com.bibliotecaviva.dto.LoginResponse;
import br.com.bibliotecaviva.exception.CredenciaisInvalidasException;
import br.com.bibliotecaviva.exception.UsuarioInativoException;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        if (!usuario.isAtivo()) {
            throw new UsuarioInativoException();
        }

        return new LoginResponse(
                tokenService.gerar(usuario),
                "Bearer",
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil());
    }
}
