package br.com.bibliotecaviva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.bibliotecaviva.model.Aviso.ReferenciaTipo;
import br.com.bibliotecaviva.model.Aviso.TipoAviso;
import br.com.bibliotecaviva.repository.AvisoRepository;
import br.com.bibliotecaviva.repository.AvisoRepository.AlvoEmprestimo;
import br.com.bibliotecaviva.repository.AvisoRepository.AlvoReserva;
import br.com.bibliotecaviva.repository.AvisoRepository.PosicaoFila;

class AvisoVarreduraServiceTest {

    private final AvisoRepository repository = mock(AvisoRepository.class);
    private final AvisoService avisoService = mock(AvisoService.class);
    // 2026-10-01T13:00Z → 10:00 em São Paulo → hoje = 2026-10-01, amanhã = 2026-10-02.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-01T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private final AvisoVarreduraService service = new AvisoVarreduraService(repository, avisoService, clock);

    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private final LocalDate amanha = LocalDate.of(2026, 10, 2);

    @BeforeEach void preparar() {
        when(avisoService.criar(any(), any(), anyString(), anyString(), any(), any(), any()))
                .thenReturn(Optional.of(1L));
        when(repository.emprestimosComPrevista(amanha)).thenReturn(List.of());
        when(repository.emprestimosAtrasados(hoje)).thenReturn(List.of());
        when(repository.reservasDisponiveisComLimite(amanha)).thenReturn(List.of());
        when(repository.reservasExpiradas()).thenReturn(List.of());
        when(repository.posicoesDaFila()).thenReturn(List.of());
    }

    @Test void emiteDevidaAmanhaParaVencimentoDeAmanha() {
        when(repository.emprestimosComPrevista(amanha))
                .thenReturn(List.of(new AlvoEmprestimo(10L, 50L, amanha)));
        assertEquals(1, service.executar().devidaAmanha());
        verify(avisoService).criar(eq(50L), eq(TipoAviso.DEVIDA_AMANHA), anyString(), anyString(),
                eq(ReferenciaTipo.emprestimo), eq(10L), eq(hoje));
    }

    @Test void naoEmiteDevidaAmanhaParaVencimentoDepoisDeAmanha() {
        // O repositório já filtra por data_prevista = amanhã; aqui só não devolve alvos.
        assertEquals(0, service.executar().devidaAmanha());
        verify(avisoService, never()).criar(any(), eq(TipoAviso.DEVIDA_AMANHA), anyString(), anyString(),
                any(), any(), any());
    }

    @Test void emiteVencidoParaAtrasado() {
        when(repository.emprestimosAtrasados(hoje))
                .thenReturn(List.of(new AlvoEmprestimo(10L, 50L, hoje.minusDays(3))));
        assertEquals(1, service.executar().vencido());
        verify(avisoService).criar(eq(50L), eq(TipoAviso.VENCIDO), anyString(), anyString(),
                eq(ReferenciaTipo.emprestimo), eq(10L), eq(hoje));
    }

    @Test void emiteReservaExpirandoParaLimiteAmanha() {
        when(repository.reservasDisponiveisComLimite(amanha))
                .thenReturn(List.of(new AlvoReserva(7L, 50L, OffsetDateTime.now(clock).plusDays(1))));
        assertEquals(1, service.executar().reservaExpirando());
        verify(avisoService).criar(eq(50L), eq(TipoAviso.RESERVA_EXPIRANDO), anyString(), anyString(),
                eq(ReferenciaTipo.reserva), eq(7L), eq(hoje));
    }

    @Test void emiteReservaExpiradaUmaVezSemDiaDeReferencia() {
        when(repository.reservasExpiradas())
                .thenReturn(List.of(new AlvoReserva(7L, 50L, OffsetDateTime.now(clock).minusDays(1))));
        assertEquals(1, service.executar().reservaExpirada());
        // diaReferencia null → 1 aviso por reserva, para sempre (não reemite todo dia).
        verify(avisoService).criar(eq(50L), eq(TipoAviso.RESERVA_EXPIRADA), anyString(), anyString(),
                eq(ReferenciaTipo.reserva), eq(7L), isNull());
    }

    @Test void filaBaselineNaoEmiteMasGravaPosicao() {
        when(repository.posicoesDaFila()).thenReturn(List.of(new PosicaoFila(7L, 50L, 3, null)));
        assertEquals(0, service.executar().filaPosicao());
        verify(avisoService, never()).criar(any(), eq(TipoAviso.FILA_POSICAO), anyString(), anyString(),
                any(), any(), any());
        verify(repository).gravarUltimaPosicao(7L, 3);
    }

    @Test void filaEmiteQuandoPosicaoDiminui() {
        when(repository.posicoesDaFila()).thenReturn(List.of(new PosicaoFila(7L, 50L, 2, 5)));
        assertEquals(1, service.executar().filaPosicao());
        verify(avisoService).criar(eq(50L), eq(TipoAviso.FILA_POSICAO), anyString(), anyString(),
                eq(ReferenciaTipo.reserva), eq(7L), eq(hoje));
        verify(repository).gravarUltimaPosicao(7L, 2);
    }

    @Test void filaNaoEmiteQuandoPosicaoMantem() {
        when(repository.posicoesDaFila()).thenReturn(List.of(new PosicaoFila(7L, 50L, 3, 3)));
        assertEquals(0, service.executar().filaPosicao());
        verify(avisoService, never()).criar(any(), eq(TipoAviso.FILA_POSICAO), anyString(), anyString(),
                any(), any(), any());
    }

    @Test void totalSomaOsCincoTipos() {
        when(repository.emprestimosComPrevista(amanha))
                .thenReturn(List.of(new AlvoEmprestimo(10L, 50L, amanha)));
        when(repository.emprestimosAtrasados(hoje))
                .thenReturn(List.of(new AlvoEmprestimo(11L, 50L, hoje.minusDays(1))));
        var r = service.executar();
        assertEquals(2, r.total());
        assertEquals(1, r.devidaAmanha());
        assertEquals(1, r.vencido());
    }
}