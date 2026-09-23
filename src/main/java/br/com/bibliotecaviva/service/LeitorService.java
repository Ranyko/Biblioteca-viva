package br.com.bibliotecaviva.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.LeitorCreateRequest;
import br.com.bibliotecaviva.dto.LeitorResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.model.Leitor;
import br.com.bibliotecaviva.model.PerfilUsuario;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.LeitorRepository;

@Service
public class LeitorService {

    private final LeitorRepository leitorRepository;
    private final UsuarioService usuarioService;

    public LeitorService(LeitorRepository leitorRepository, UsuarioService usuarioService) {
        this.leitorRepository = leitorRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<LeitorResponse> listar() {
        return leitorRepository.findAll(Sort.by(Sort.Direction.ASC, "usuario.nome"))
                .stream()
                .map(LeitorResponse::from)
                .toList();
    }

    @Transactional
    public LeitorResponse criar(LeitorCreateRequest request) {
        String documento = request.documento().replaceAll("[^0-9A-Za-z]", "");
        if (documento.isBlank()) {
            throw new IllegalArgumentException("O documento deve conter letras ou números");
        }
        if (leitorRepository.existsByDocumento(documento)) {
            throw new ConflitoException("Já existe um leitor com este documento");
        }

        Usuario usuario = usuarioService.criarEntidade(
                request.nome(),
                request.email(),
                request.senha(),
                PerfilUsuario.LEITOR,
                true);

        Leitor leitor = new Leitor();
        leitor.setUsuario(usuario);
        leitor.setDocumento(documento);
        leitor.setTelefone(request.telefone() == null ? null : request.telefone().trim());
        return LeitorResponse.from(leitorRepository.save(leitor));
    }
}
