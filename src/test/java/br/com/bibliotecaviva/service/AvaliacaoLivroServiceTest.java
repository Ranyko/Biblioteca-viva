package br.com.bibliotecaviva.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.bibliotecaviva.dto.AvaliacaoRequest;
import br.com.bibliotecaviva.dto.AvaliacaoResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.repository.AvaliacaoLivroRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvaliacaoLivroServiceTest {
    private final AvaliacaoLivroRepository repository = mock(AvaliacaoLivroRepository.class);
    private final AvaliacaoLivroService service = new AvaliacaoLivroService(repository);

    @Test
    void criaAvaliacaoComResenhaNormalizada() {
        when(repository.livroAtivo(7)).thenReturn(true);
        when(repository.temEmprestimoDevolvido(7, 12)).thenReturn(true);
        when(repository.existeAvaliacao(7, 12)).thenReturn(false);
        AvaliacaoResponse esperada = new AvaliacaoResponse(1, 7, 5, "Boa leitura", null);
        when(repository.criar(7, 12, 5, "Boa leitura")).thenReturn(esperada);

        assertEquals(esperada, service.criar(7, 12, new AvaliacaoRequest(5, "  Boa leitura  ")));
    }

    @Test
    void resenhaEmBrancoEhArmazenadaComoNula() {
        when(repository.livroAtivo(7)).thenReturn(true);
        when(repository.temEmprestimoDevolvido(7, 12)).thenReturn(true);
        when(repository.existeAvaliacao(7, 12)).thenReturn(false);
        when(repository.criar(7, 12, 4, null)).thenReturn(new AvaliacaoResponse(1, 7, 4, null, null));

        service.criar(7, 12, new AvaliacaoRequest(4, "   "));

        verify(repository).criar(7, 12, 4, null);
    }

    @Test
    void naoCriaAvaliacaoParaLivroInativo() {
        when(repository.livroAtivo(7)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.criar(7, 12, new AvaliacaoRequest(5, null)));
        verify(repository, never()).temEmprestimoDevolvido(anyLong(), anyLong());
    }

    @Test
    void naoPermiteCriarSegundaAvaliacao() {
        when(repository.livroAtivo(7)).thenReturn(true);
        when(repository.existeAvaliacao(7, 12)).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.criar(7, 12, new AvaliacaoRequest(5, null)));
        verify(repository, never()).criar(anyLong(), anyLong(), anyInt(), any());
    }

    @Test
    void exigeEmprestimoDevolvido() {
        when(repository.livroAtivo(7)).thenReturn(true);
        when(repository.existeAvaliacao(7, 12)).thenReturn(false);
        when(repository.temEmprestimoDevolvido(7, 12)).thenReturn(false);

        assertThrows(ConflitoException.class,
                () -> service.criar(7, 12, new AvaliacaoRequest(5, null)));
        verify(repository, never()).criar(anyLong(), anyLong(), anyInt(), any());
    }

    @Test
    void consultaPropriaExigeAvaliacaoExistente() {
        when(repository.livroAtivo(7)).thenReturn(true);
        when(repository.buscarPropria(7, 12)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPropria(7, 12));
    }

    @Test
    void atualizaSomenteAvaliacaoPropriaExistente() {
        when(repository.livroAtivo(7)).thenReturn(true);
        when(repository.atualizar(7, 12, 3, null)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.atualizar(7, 12, new AvaliacaoRequest(3, "  ")));
        verify(repository).atualizar(7, 12, 3, null);
    }

    @Test
    void exclusaoDeAvaliacaoAusenteRetornaNaoEncontrado() {
        when(repository.livroAtivo(7)).thenReturn(true);
        when(repository.excluir(7, 12)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(7, 12));
    }

    @Test
    void rejeitaPaginacaoInvalidaSemConsultarRepositorio() {
        assertThrows(IllegalArgumentException.class, () -> service.listar(7, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listar(7, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> service.listar(7, 0, 101));
        verifyNoInteractions(repository);
    }
}
