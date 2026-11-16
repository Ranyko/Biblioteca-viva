package br.com.bibliotecaviva.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bibliotecaviva.dto.MovimentacaoCsvResponse;
import br.com.bibliotecaviva.repository.RelatorioMovimentacaoRepository;

@ExtendWith(MockitoExtension.class)
class RelatorioMovimentacaoServiceTest {

    @Mock
    private RelatorioMovimentacaoRepository repository;

    private RelatorioMovimentacaoService service;

    @BeforeEach
    void setUp() {
        service = new RelatorioMovimentacaoService(repository);
    }

    @Test
    void deveExportarCabecalhoQuandoNaoExistemMovimentacoes() {
        when(repository.buscarMovimentacoes(null, null))
                .thenReturn(List.of());

        byte[] resultado = service.exportarCsv(null, null);

        byte[] bomUtf8 = {
                (byte) 0xEF,
                (byte) 0xBB,
                (byte) 0xBF
        };

        assertArrayEquals(
                bomUtf8,
                new byte[] { resultado[0], resultado[1], resultado[2] });

        String csv = new String(
                resultado, 3, resultado.length - 3, StandardCharsets.UTF_8);

        assertEquals(
                "Período;Obra;Leitor;Retirada;Prazo;Devolução;Situação\n",
                csv);

        verify(repository).buscarMovimentacoes(null, null);
    }

    @Test
    void deveExportarEmprestimoEmAndamento() {
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fim = LocalDate.of(2026, 10, 31);

        MovimentacaoCsvResponse movimentacao = new MovimentacaoCsvResponse(
                "Dom Casmurro",
                "Ana Silva",
                OffsetDateTime.parse("2026-10-12T14:30:00-03:00"),
                LocalDate.of(2026, 11, 11),
                null);

        when(repository.buscarMovimentacoes(inicio, fim))
                .thenReturn(List.of(movimentacao));

        byte[] resultado = service.exportarCsv(inicio, fim);

        String csv = new String(
                resultado, 3, resultado.length - 3, StandardCharsets.UTF_8);

        assertTrue(csv.contains("01/10/2026 a 31/10/2026"));
        assertTrue(csv.contains("\"Dom Casmurro\""));
        assertTrue(csv.contains("\"Ana Silva\""));
        assertTrue(csv.contains("\"12/10/2026 14:30\""));
        assertTrue(csv.contains("\"11/11/2026\""));
        assertTrue(csv.contains("\"\""));
        assertTrue(csv.contains("\"EM ANDAMENTO\""));

        verify(repository).buscarMovimentacoes(inicio, fim);
    }

    @Test
    void deveExportarEmprestimoDevolvido() {
        MovimentacaoCsvResponse movimentacao = new MovimentacaoCsvResponse(
                "O Alienista",
                "Carlos Souza",
                OffsetDateTime.parse("2026-10-10T09:15:00-03:00"),
                LocalDate.of(2026, 11, 9),
                OffsetDateTime.parse("2026-10-20T16:45:00-03:00"));

        when(repository.buscarMovimentacoes(null, null))
                .thenReturn(List.of(movimentacao));

        byte[] resultado = service.exportarCsv(null, null);

        String csv = new String(
                resultado, 3, resultado.length - 3, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Todos os períodos\""));
        assertTrue(csv.contains("\"O Alienista\""));
        assertTrue(csv.contains("\"10/10/2026 09:15\""));
        assertTrue(csv.contains("\"09/11/2026\""));
        assertTrue(csv.contains("\"20/10/2026 16:45\""));
        assertTrue(csv.contains("\"DEVOLVIDO\""));
    }

    @Test
    void deveFormatarPeriodoQuandoApenasDataInicialEstaInformada() {
        LocalDate inicio = LocalDate.of(2026, 10, 1);

        when(repository.buscarMovimentacoes(inicio, null))
                .thenReturn(List.of());

        byte[] resultado = service.exportarCsv(inicio, null);

        String csv = new String(
                resultado, 3, resultado.length - 3, StandardCharsets.UTF_8);

        assertTrue(csv.startsWith(
                "Período;Obra;Leitor;Retirada;Prazo;Devolução;Situação\n"));
        verify(repository).buscarMovimentacoes(inicio, null);
    }

    @Test
    void deveRejeitarPeriodoComDataInicialPosteriorADataFinal() {
        LocalDate inicio = LocalDate.of(2026, 10, 31);
        LocalDate fim = LocalDate.of(2026, 10, 1);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.exportarCsv(inicio, fim));

        assertEquals(
                "A data inicial não pode ser posterior à data final.",
                erro.getMessage());

        verifyNoInteractions(repository);
    }

    @Test
    void deveEscaparAspasNoConteudoDoCsv() {
        MovimentacaoCsvResponse movimentacao = new MovimentacaoCsvResponse(
                "Livro \"Especial\"",
                "João Silva",
                OffsetDateTime.parse("2026-10-10T09:15:00-03:00"),
                LocalDate.of(2026, 11, 9),
                null);

        when(repository.buscarMovimentacoes(null, null))
                .thenReturn(List.of(movimentacao));

        byte[] resultado = service.exportarCsv(null, null);

        String csv = new String(
                resultado, 3, resultado.length - 3, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Livro \"\"Especial\"\"\""));
    }
}