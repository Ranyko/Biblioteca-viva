package br.com.bibliotecaviva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bibliotecaviva.dto.EmprestimoLeitorResponse;
import br.com.bibliotecaviva.dto.MultaLeitorResponse;
import br.com.bibliotecaviva.dto.PainelLeitorResponse;
import br.com.bibliotecaviva.model.Livro;
import br.com.bibliotecaviva.model.Reserva;
import br.com.bibliotecaviva.model.StatusReserva;
import br.com.bibliotecaviva.repository.PainelLeitorRepository;
import br.com.bibliotecaviva.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class PainelLeitorServiceTest {

    @Mock
    private PainelLeitorRepository painelLeitorRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private PainelLeitorService service;

    @BeforeEach
    void setUp() {
        service = new PainelLeitorService(
                painelLeitorRepository,
                reservaRepository);
    }

    @Test
    void deveReunirEmprestimosReservasEMultasDoLeitor() {
        Long leitorId = 20L;

        EmprestimoLeitorResponse emprestimo =
                new EmprestimoLeitorResponse(
                        1L, "Livro de Teste", null, null, null, "ATIVO");

        MultaLeitorResponse multa =
                new MultaLeitorResponse(
                        2L, null, "PENDENTE", null, null);

        Livro livro = mock(Livro.class);
        when(livro.getId()).thenReturn(10L);
        when(livro.getTitulo()).thenReturn("Livro de Teste");

        Reserva reserva = mock(Reserva.class);
        when(reserva.getId()).thenReturn(3L);
        when(reserva.getLivro()).thenReturn(livro);
        when(reserva.getStatus()).thenReturn(StatusReserva.ATIVA);
        when(reserva.getDataLimiteRetirada()).thenReturn(null);

        when(painelLeitorRepository.listarEmprestimos(leitorId))
                .thenReturn(List.of(emprestimo));
        when(painelLeitorRepository.listarMultas(leitorId))
                .thenReturn(List.of(multa));
        when(reservaRepository
                .findByLeitorIdOrderByDataSolicitacaoDesc(leitorId))
                .thenReturn(List.of(reserva));
        when(reservaRepository
                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                        10L, StatusReserva.ATIVA))
                .thenReturn(List.of(reserva));

        PainelLeitorResponse resultado = service.consultar(leitorId);

        assertEquals(1, resultado.emprestimos().size());
        assertEquals(1, resultado.reservas().size());
        assertEquals(1, resultado.multas().size());
        assertEquals("Livro de Teste",
                resultado.reservas().get(0).livroTitulo());
        assertEquals(1, resultado.reservas().get(0).posicaoFila());

        verify(painelLeitorRepository).listarEmprestimos(leitorId);
        verify(painelLeitorRepository).listarMultas(leitorId);
    }

    @Test
    void deveCalcularPosicaoDaReservaNaFila() {
        Long leitorId = 20L;

        Livro livro = mock(Livro.class);
        when(livro.getId()).thenReturn(10L);
        when(livro.getTitulo()).thenReturn("Livro de Teste");

        Reserva primeira = mock(Reserva.class);
        when(primeira.getId()).thenReturn(100L);

        Reserva segunda = mock(Reserva.class);
        when(segunda.getId()).thenReturn(200L);
        when(segunda.getLivro()).thenReturn(livro);
        when(segunda.getStatus()).thenReturn(StatusReserva.ATIVA);
        when(segunda.getDataLimiteRetirada()).thenReturn(null);

        when(painelLeitorRepository.listarEmprestimos(leitorId))
                .thenReturn(List.of());
        when(painelLeitorRepository.listarMultas(leitorId))
                .thenReturn(List.of());
        when(reservaRepository
                .findByLeitorIdOrderByDataSolicitacaoDesc(leitorId))
                .thenReturn(List.of(segunda));
        when(reservaRepository
                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                        10L, StatusReserva.ATIVA))
                .thenReturn(List.of(primeira, segunda));

        PainelLeitorResponse resultado = service.consultar(leitorId);

        assertEquals(2, resultado.reservas().get(0).posicaoFila());
    }

    @Test
    void reservaNaoAtivaNaoDeveTerPosicaoNaFila() {
        Long leitorId = 20L;

        Livro livro = mock(Livro.class);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getId()).thenReturn(3L);
        when(reserva.getLivro()).thenReturn(livro);
        when(reserva.getStatus()).thenReturn(StatusReserva.ATENDIDA);
        when(reserva.getDataLimiteRetirada()).thenReturn(null);

        when(painelLeitorRepository.listarEmprestimos(leitorId))
                .thenReturn(List.of());
        when(painelLeitorRepository.listarMultas(leitorId))
                .thenReturn(List.of());
        when(reservaRepository
                .findByLeitorIdOrderByDataSolicitacaoDesc(leitorId))
                .thenReturn(List.of(reserva));

        PainelLeitorResponse resultado = service.consultar(leitorId);

        assertNull(resultado.reservas().get(0).posicaoFila());
    }

    @Test
    void deveRetornarPainelComListasVazias() {
        Long leitorId = 20L;

        when(painelLeitorRepository.listarEmprestimos(leitorId))
                .thenReturn(List.of());
        when(painelLeitorRepository.listarMultas(leitorId))
                .thenReturn(List.of());
        when(reservaRepository
                .findByLeitorIdOrderByDataSolicitacaoDesc(leitorId))
                .thenReturn(List.of());

        PainelLeitorResponse resultado = service.consultar(leitorId);

        assertTrue(resultado.emprestimos().isEmpty());
        assertTrue(resultado.reservas().isEmpty());
        assertTrue(resultado.multas().isEmpty());
    }
}