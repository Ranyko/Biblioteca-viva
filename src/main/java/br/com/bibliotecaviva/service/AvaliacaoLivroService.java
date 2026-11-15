package br.com.bibliotecaviva.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.AvaliacaoRequest;
import br.com.bibliotecaviva.dto.AvaliacaoResponse;
import br.com.bibliotecaviva.dto.AvaliacoesLivroResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.repository.AvaliacaoLivroRepository;

@Service
public class AvaliacaoLivroService {
    private static final int MAX_TAMANHO_RESENHA = 1000;

    private final AvaliacaoLivroRepository repository;

    public AvaliacaoLivroService(AvaliacaoLivroRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AvaliacaoResponse criar(long livroId, long usuarioId, AvaliacaoRequest request) {
        exigirLivroAtivo(livroId);
        if (repository.existeAvaliacao(livroId, usuarioId)) {
            throw new ConflitoException("O leitor já avaliou este livro");
        }
        if (!repository.temEmprestimoDevolvido(livroId, usuarioId)) {
            throw new ConflitoException("É necessário ter devolvido um exemplar deste livro para avaliá-lo");
        }
        return repository.criar(livroId, usuarioId, request.nota(), normalizarResenha(request.resenha()));
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarPropria(long livroId, long usuarioId) {
        exigirLivroAtivo(livroId);
        return repository.buscarPropria(livroId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada"));
    }

    @Transactional
    public AvaliacaoResponse atualizar(long livroId, long usuarioId, AvaliacaoRequest request) {
        exigirLivroAtivo(livroId);
        return repository.atualizar(livroId, usuarioId, request.nota(), normalizarResenha(request.resenha()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada"));
    }

    @Transactional
    public void excluir(long livroId, long usuarioId) {
        exigirLivroAtivo(livroId);
        if (!repository.excluir(livroId, usuarioId)) {
            throw new RecursoNaoEncontradoException("Avaliação não encontrada");
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AvaliacoesLivroResponse listar(long livroId, int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        exigirLivroAtivo(livroId);
        return repository.listar(livroId, pagina, tamanho);
    }

    private void exigirLivroAtivo(long livroId) {
        if (!repository.livroAtivo(livroId)) {
            throw new RecursoNaoEncontradoException("Livro não encontrado");
        }
    }

    private static String normalizarResenha(String resenha) {
        if (resenha == null) return null;
        String normalizada = resenha.trim();

        if (normalizada.length() > MAX_TAMANHO_RESENHA) {
            throw new IllegalArgumentException("Resenha deve ter no máximo " + MAX_TAMANHO_RESENHA + " caracteres");
        }

        return normalizada.isEmpty() ? null : normalizada;
    }

    private static void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Página deve ser >= 0 e tamanho deve estar entre 1 e 100");
        }
    }
}
