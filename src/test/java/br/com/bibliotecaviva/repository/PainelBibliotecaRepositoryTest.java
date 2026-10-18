
package br.com.bibliotecaviva.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import br.com.bibliotecaviva.dto.EmprestimosPorCategoriaResponse;
import br.com.bibliotecaviva.dto.ObraMaisEmprestadaResponse;

@ExtendWith(MockitoExtension.class)
class PainelBibliotecaRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PainelBibliotecaRepository repository;

    @Test
    void deveContarEmprestimosAtivos() {
        when(jdbcTemplate.queryForObject(
                contains("data_devolucao IS NULL"),
                eq(Long.class)))
                .thenReturn(3L);

        assertThat(repository.contarEmprestimosAtivos()).isEqualTo(3L);
    }

    @Test
    void deveContarEmprestimosAtrasados() {
        when(jdbcTemplate.queryForObject(
                contains("data_prevista_devolucao < CURRENT_DATE"),
                eq(Long.class)))
                .thenReturn(2L);

        assertThat(repository.contarEmprestimosAtrasados()).isEqualTo(2L);
    }

    @Test
    void deveBuscarRankingDeObras() {
        List<ObraMaisEmprestadaResponse> esperado = List.of(
                new ObraMaisEmprestadaResponse(1L, "Livro A", 5L),
                new ObraMaisEmprestadaResponse(2L, "Livro B", 3L));

        when(jdbcTemplate.query(
                contains("ORDER BY COUNT(e.id) DESC"),
                ArgumentMatchers.<RowMapper<ObraMaisEmprestadaResponse>>any()))
                .thenReturn(esperado);

        assertThat(repository.buscarRankingObras()).containsExactlyElementsOf(esperado);
    }

    @Test
    void deveBuscarEmprestimosPorCategoria() {
        List<EmprestimosPorCategoriaResponse> esperado = List.of(
                new EmprestimosPorCategoriaResponse(1L, "Fantasia", 4L),
                new EmprestimosPorCategoriaResponse(2L, "Romance", 2L));

        when(jdbcTemplate.query(
                contains("COUNT(DISTINCT e.id)"),
                ArgumentMatchers.<RowMapper<EmprestimosPorCategoriaResponse>>any()))
                .thenReturn(esperado);

        assertThat(repository.buscarEmprestimosPorCategoria())
                .containsExactlyElementsOf(esperado);
    }
}
