package br.com.bibliotecaviva.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.UsuarioCreateRequest;
import br.com.bibliotecaviva.dto.UsuarioResponse;
import br.com.bibliotecaviva.dto.UsuarioUpdateRequest;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.model.PerfilUsuario;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"))
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest request) {
        if (request.perfil() == PerfilUsuario.LEITOR) {
            throw new IllegalArgumentException(
                    "Leitores devem ser cadastrados pelo fluxo do atendente");
        }
        return UsuarioResponse.from(criarEntidade(
                request.nome(),
                request.email(),
                request.senha(),
                request.perfil(),
                request.ativo() == null || request.ativo()));
    }

    @Transactional
    public Usuario criarEntidade(
            String nome,
            String email,
            String senha,
            PerfilUsuario perfil,
            boolean ativo) {
        String emailNormalizado = normalizarEmail(email);
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ConflitoException("Já existe um usuário com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarEntidade(id);
        String emailNormalizado = normalizarEmail(request.email());

        boolean mudancaEnvolveLeitor = (usuario.getPerfil() == PerfilUsuario.LEITOR)
                ^ (request.perfil() == PerfilUsuario.LEITOR);
        if (mudancaEnvolveLeitor) {
            throw new IllegalArgumentException(
                    "Não é permitido converter um leitor em funcionário ou um funcionário em leitor");
        }

        usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new ConflitoException("Já existe um usuário com este e-mail");
                });

        usuario.setNome(request.nome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(request.ativo());
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
