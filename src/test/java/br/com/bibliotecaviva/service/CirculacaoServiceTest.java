package br.com.bibliotecaviva.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.repository.CirculacaoRepository;
import br.com.bibliotecaviva.repository.CirculacaoRepository.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CirculacaoServiceTest {
    private final CirculacaoRepository repository = mock(CirculacaoRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-01T02:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private final OffsetDateTime agora = OffsetDateTime.now(clock);
    private final CirculacaoService service = new CirculacaoService(repository, clock);
    private final EmprestimoCreateRequest request = new EmprestimoCreateRequest(1L, 2L);

    @BeforeEach void preparar() {
        when(repository.travarExemplar(2)).thenReturn(new ExemplarDados(2, 1, true, "disponivel"));
        when(repository.travarLeitor(1)).thenReturn(new LeitorDados(1, true, "leitor"));
        when(repository.regras()).thenReturn(new Regras(14, 3, new BigDecimal("2.50"), new BigDecimal("25.00"), 2));
        when(repository.reservaDoExemplar(2)).thenReturn(Optional.empty());
        when(repository.primeiraReservaElegivel(1, 3)).thenReturn(Optional.empty());
    }

    @Test void calculaPrazoNoFusoDaBiblioteca() {
        service.emprestar(request, 4);
        verify(repository).criar(1, 2, 4, agora, LocalDate.of(2026, 10, 14)); // Ainda é 30/09 em São Paulo.
        verify(repository).statusExemplar(2, "emprestado");
    }
    @Test void rejeitaLeitorInativo() {
        when(repository.travarLeitor(1)).thenReturn(new LeitorDados(1, false, "leitor"));
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
        verify(repository, never()).criar(anyLong(), anyLong(), anyLong(), any(), any());
    }
    @Test void rejeitaPerfilAlterado() {
        when(repository.travarLeitor(1)).thenReturn(new LeitorDados(1, true, "administrador"));
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    @Test void rejeitaLimiteAtingido() {
        when(repository.ativosDoLeitor(1)).thenReturn(3L);
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    @Test void rejeitaExemplarEmprestado() {
        when(repository.travarExemplar(2)).thenReturn(new ExemplarDados(2, 1, true, "emprestado"));
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    @Test void rejeitaExemplarInativo() {
        when(repository.travarExemplar(2)).thenReturn(new ExemplarDados(2, 1, true, "inativo"));
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    @Test void rejeitaLivroInativo() {
        when(repository.travarExemplar(2)).thenReturn(new ExemplarDados(2, 1, false, "disponivel"));
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    @Test void naoIgnoraEmprestimoMesmoSeStatusInconsistente() {
        when(repository.temEmprestimoAtivo(2)).thenReturn(true);
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    @Test void protegeFilaExistente() {
        when(repository.primeiraReservaElegivel(1, 3)).thenReturn(Optional.of(8L));
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    private void reserva(long leitorId, OffsetDateTime limite) {
        when(repository.travarExemplar(2)).thenReturn(new ExemplarDados(2, 1, true, "reservado"));
        when(repository.reservaDoExemplar(2)).thenReturn(Optional.of(new ReservaDados(7, leitorId, limite)));
    }
    @Test void retiraReservaPropria() {
        reserva(1, agora.plusDays(1));
        service.emprestar(request, 4);
        verify(repository).atenderReserva(7);
    }
    @Test void rejeitaReservaDeOutroLeitor() {
        reserva(2, agora.plusDays(1));
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    @Test void rejeitaReservaNoInstanteDaExpiracao() {
        reserva(1, agora);
        assertThrows(ConflitoException.class, () -> service.emprestar(request, 4));
    }
    private void emprestimo(LocalDate prevista, OffsetDateTime devolucao) {
        var em = new EmprestimoResponse(10L, 1L, 2L, 4L, null, agora.minusDays(30), prevista, devolucao);
        when(repository.buscar(10)).thenReturn(em);
        when(repository.travarEmprestimo(10)).thenReturn(em);
        when(repository.travarExemplar(2)).thenReturn(new ExemplarDados(2, 1, true, "emprestado"));
    }
    @Test void devolveNoPrazoSemMulta() {
        emprestimo(agora.toLocalDate(), null);
        var result = service.devolver(10, 4);
        assertEquals(0, result.diasAtraso());
        assertEquals(new BigDecimal("0.00"), result.valorMulta());
        assertEquals("disponivel", result.statusExemplar());
        verify(repository, never()).gerarMulta(anyLong(), anyInt(), any(), any());
    }
    @Test void calculaAtrasoEMulta() {
        emprestimo(agora.toLocalDate().minusDays(5), null);
        var result = service.devolver(10, 4);
        assertEquals(5, result.diasAtraso());
        assertEquals(new BigDecimal("12.50"), result.valorMulta());
        verify(repository).gerarMulta(eq(10L), eq(5), any(), eq(agora));
    }
    @Test void respeitaTeto() {
        emprestimo(agora.toLocalDate().minusDays(20), null);
        assertEquals(new BigDecimal("25.00"), service.devolver(10, 4).valorMulta());
    }
    @Test void tetoZeroNaoCriaMulta() {
        emprestimo(agora.toLocalDate().minusDays(5), null);
        when(repository.regras()).thenReturn(new Regras(14, 3, new BigDecimal("2.50"), BigDecimal.ZERO, 2));
        assertEquals(new BigDecimal("0.00"), service.devolver(10, 4).valorMulta());
        verify(repository, never()).gerarMulta(anyLong(), anyInt(), any(), any());
    }
    @Test void diariaZeroNaoCriaMulta() {
        emprestimo(agora.toLocalDate().minusDays(5), null);
        when(repository.regras()).thenReturn(new Regras(14, 3, BigDecimal.ZERO, new BigDecimal("25.00"), 2));
        service.devolver(10, 4);
        verify(repository, never()).gerarMulta(anyLong(), anyInt(), any(), any());
    }
    @Test void devolucaoDisponibilizaPrimeiroElegivel() {
        emprestimo(agora.toLocalDate(), null);
        when(repository.primeiraReservaElegivel(1, 3)).thenReturn(Optional.of(7L));
        var result = service.devolver(10, 4);
        assertEquals("reservado", result.statusExemplar());
        assertEquals(7L, result.reservaDisponibilizadaId());
        verify(repository).disponibilizarReserva(7, 2, agora, agora.plusDays(2));
    }
    @Test void rejeitaSegundaDevolucao() {
        emprestimo(agora.toLocalDate(), agora);
        assertThrows(ConflitoException.class, () -> service.devolver(10, 4));
        verify(repository, never()).devolver(anyLong(), anyLong(), any());
    }
    @Test void devolveObraInativaSemDisponibilizarReserva() {
        emprestimo(agora.toLocalDate(), null);
        when(repository.travarExemplar(2)).thenReturn(new ExemplarDados(2, 1, false, "emprestado"));
        when(repository.primeiraReservaElegivel(1, 3)).thenReturn(Optional.of(7L));
        assertEquals("disponivel", service.devolver(10, 4).statusExemplar());
        verify(repository, never()).disponibilizarReserva(anyLong(), anyLong(), any(), any());
    }
}
