package br.com.bibliotecaviva.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import br.com.bibliotecaviva.dto.SugestaoLivroResponse;

@ExtendWith(MockitoExtension.class)
class SugestaoLivroRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SugestaoLivroRepository repository;

    @Test
    void deveBuscarSugestoesDoLeitor() {
        Long leitorId = 1L;

        List<SugestaoLivroResponse> esperadas = List.of(
                new SugestaoLivroResponse(
                        10L, "Livro A", "Descrição A", "Fantasia", 3L),
                new SugestaoLivroResponse(
                        20L, "Livro B", "Descrição B", "Romance", 1L));

        when(jdbcTemplate.query(
                contains("WITH categorias_leitor AS"),
                ArgumentMatchers.<RowMapper<SugestaoLivroResponse>>any(),
                eq(leitorId),
                eq(leitorId)))
                .thenReturn(esperadas);

        List<SugestaoLivroResponse> resultado =
                repository.buscarSugestoes(leitorId);

        assertThat(resultado).containsExactlyElementsOf(esperadas);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremSugestoes() {
        Long leitorId = 1L;

        when(jdbcTemplate.query(
                contains("WITH categorias_leitor AS"),
                ArgumentMatchers.<RowMapper<SugestaoLivroResponse>>any(),
                eq(leitorId),
                eq(leitorId)))
                .thenReturn(List.of());

        assertThat(repository.buscarSugestoes(leitorId)).isEmpty();
    }

    @Test
    void deveConsultarHistoricoDoLeitorEExcluirLivrosJaEmprestados() {
        Long leitorId = 7L;

        when(jdbcTemplate.query(
                contains("NOT EXISTS"),
                ArgumentMatchers.<RowMapper<SugestaoLivroResponse>>any(),
                eq(leitorId),
                eq(leitorId)))
                .thenReturn(List.of());

        repository.buscarSugestoes(leitorId);

        verify(jdbcTemplate).query(
                contains("NOT EXISTS"),
                ArgumentMatchers.<RowMapper<SugestaoLivroResponse>>any(),
                eq(leitorId),
                eq(leitorId));
    }
}