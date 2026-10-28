
package br.com.bibliotecaviva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bibliotecaviva.dto.EmprestimosPorCategoriaResponse;
import br.com.bibliotecaviva.dto.ObraMaisEmprestadaResponse;
import br.com.bibliotecaviva.dto.PainelBibliotecaResponse;
import br.com.bibliotecaviva.repository.PainelBibliotecaRepository;

@ExtendWith(MockitoExtension.class)
class PainelBibliotecaServiceTest {

    @Mock
    private PainelBibliotecaRepository repository;

    @InjectMocks
    private PainelBibliotecaService service;

    @Test
    void deveMontarPainelComDadosDoRepositorio() {
        var ranking = List.of(
            new ObraMaisEmprestadaResponse(1L, "Livro A", 5L)
        );

        var categorias = List.of(
            new EmprestimosPorCategoriaResponse(1L, "Fantasia", 3L)
        );

        when(repository.contarEmprestimosAtivos()).thenReturn(4L);
        when(repository.contarEmprestimosAtrasados()).thenReturn(2L);
        when(repository.buscarRankingObras()).thenReturn(ranking);
        when(repository.buscarEmprestimosPorCategoria()).thenReturn(categorias);

        PainelBibliotecaResponse resposta = service.consultar();

        assertThat(resposta.emprestimosAtivos()).isEqualTo(4L);
        assertThat(resposta.emprestimosAtrasados()).isEqualTo(2L);
        assertThat(resposta.rankingObras()).isEqualTo(ranking);
        assertThat(resposta.emprestimosPorCategoria()).isEqualTo(categorias);

        verify(repository).contarEmprestimosAtivos();
        verify(repository).contarEmprestimosAtrasados();
        verify(repository).buscarRankingObras();
        verify(repository).buscarEmprestimosPorCategoria();
    }
}
