package br.com.bibliotecaviva.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bibliotecaviva.dto.ReservaCreateRequest;
import br.com.bibliotecaviva.dto.ReservaResponse;
import br.com.bibliotecaviva.dto.FilaReservaResponse;
import br.com.bibliotecaviva.dto.ReservaCancelamentoResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.model.Leitor;
import br.com.bibliotecaviva.model.Livro;
import br.com.bibliotecaviva.model.StatusReserva;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.model.Reserva;
import br.com.bibliotecaviva.repository.ExemplarRepository;
import br.com.bibliotecaviva.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

        @Mock
        ReservaRepository reservaRepository;

        @Mock
        ExemplarRepository exemplarRepository;

        @Mock
        LeitorService leitorService;

        @Mock
        LivroService livroService;

        @InjectMocks
        ReservaService service;

        private Leitor leitor(boolean ativo) {

                Usuario usuario = new Usuario();
                usuario.setAtivo(ativo);
                usuario.setNome("Teste");

                Leitor leitor = mock(Leitor.class);

                when(leitor.getUsuario()).thenReturn(usuario);

                return leitor;
        }

        private Livro livro() {

                return mock(Livro.class);
        }

        @Test
        void leitorInativoNaoPodeReservar() {

                ReservaCreateRequest request = new ReservaCreateRequest(1L, 1L);

                Leitor leitor = leitor(false);

                when(leitorService.buscarEntidade(1L))
                                .thenReturn(leitor);

                assertThrows(
                                ConflitoException.class,
                                () -> service.criar(request));

                verifyNoInteractions(
                                exemplarRepository,
                                reservaRepository,
                                livroService);
        }

        @Test
        void naoPermiteReservaQuandoExisteExemplarDisponivel() {

                ReservaCreateRequest request = new ReservaCreateRequest(1L, 1L);

                Leitor leitor = leitor(true);
                Livro livro = livro();

                when(leitorService.buscarEntidade(1L))
                                .thenReturn(leitor);

                when(livroService.buscarEntidade(1L))
                                .thenReturn(livro);

                when(exemplarRepository.countByLivroIdAndStatus(
                                any(),
                                any()))
                                .thenReturn(1L);

                assertThrows(
                                ConflitoException.class,
                                () -> service.criar(request));
        }

        @Test
        void naoPermiteReservaDuplicada() {

                ReservaCreateRequest request = new ReservaCreateRequest(1L, 1L);

                Leitor leitor = leitor(true);
                Livro livro = livro();

                when(leitorService.buscarEntidade(1L))
                                .thenReturn(leitor);

                when(livroService.buscarEntidade(1L))
                                .thenReturn(livro);

                when(exemplarRepository.countByLivroIdAndStatus(
                                any(),
                                any()))
                                .thenReturn(0L);

                when(reservaRepository.existsByLeitorIdAndLivroIdAndStatusIn(
                                any(),
                                any(),
                                any()))
                                .thenReturn(true);

                assertThrows(
                                ConflitoException.class,
                                () -> service.criar(request));

                verify(reservaRepository, never())
                                .save(any());
        }

        @Test
        void criaReservaComPosicaoCorretaNaFila() {

                ReservaCreateRequest request = new ReservaCreateRequest(1L, 1L);

                Leitor leitor = leitor(true);
                Livro livro = livro();

                Reserva reservaSalva = mock(Reserva.class);

                when(leitorService.buscarEntidade(1L))
                                .thenReturn(leitor);

                when(livroService.buscarEntidade(1L))
                                .thenReturn(livro);

                when(exemplarRepository.countByLivroIdAndStatus(
                                any(),
                                any()))
                                .thenReturn(0L);

                when(reservaRepository.existsByLeitorIdAndLivroIdAndStatusIn(
                                any(),
                                any(),
                                any()))
                                .thenReturn(false);

                when(reservaRepository.save(any()))
                                .thenReturn(reservaSalva);

                when(reservaSalva.getId())
                                .thenReturn(1L);

                when(reservaSalva.getLeitor())
                                .thenReturn(leitor);

                when(reservaSalva.getLivro())
                                .thenReturn(livro);

                when(reservaSalva.getStatus())
                                .thenReturn(StatusReserva.ATIVA);

                when(reservaRepository
                                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                                any(),
                                                eq(StatusReserva.ATIVA)))
                                .thenReturn(List.of(reservaSalva));

                ReservaResponse response = service.criar(request);

                assertEquals(1, response.posicaoFila());
        }

        @Test
        void listarFilaRetornaReservasNaOrdemCorreta() {

                Leitor leitor1 = mock(Leitor.class);
                Leitor leitor2 = mock(Leitor.class);

                Usuario usuario1 = new Usuario();
                usuario1.setNome("João");

                Usuario usuario2 = new Usuario();
                usuario2.setNome("Maria");

                when(leitor1.getId()).thenReturn(1L);
                when(leitor1.getUsuario()).thenReturn(usuario1);

                when(leitor2.getId()).thenReturn(2L);
                when(leitor2.getUsuario()).thenReturn(usuario2);

                Reserva reserva1 = mock(Reserva.class);
                Reserva reserva2 = mock(Reserva.class);

                when(reserva1.getId()).thenReturn(10L);
                when(reserva1.getLeitor()).thenReturn(leitor1);

                when(reserva2.getId()).thenReturn(20L);
                when(reserva2.getLeitor()).thenReturn(leitor2);

                when(reservaRepository
                                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                                1L,
                                                StatusReserva.ATIVA))
                                .thenReturn(List.of(reserva1, reserva2));

                List<FilaReservaResponse> fila = service.listarFila(1L);

                assertEquals(2, fila.size());

                assertEquals(1, fila.get(0).posicao());
                assertEquals(10L, fila.get(0).reservaId());
                assertEquals(1L, fila.get(0).leitorId());
                assertEquals("João", fila.get(0).leitorNome());

                assertEquals(2, fila.get(1).posicao());
                assertEquals(20L, fila.get(1).reservaId());
                assertEquals(2L, fila.get(1).leitorId());
                assertEquals("Maria", fila.get(1).leitorNome());
        }

        @Test
        void cancelaReserva() {

                Reserva reserva = new Reserva();

                reserva.setStatus(StatusReserva.ATIVA);

                when(reservaRepository.findById(1L))
                                .thenReturn(java.util.Optional.of(reserva));

                ReservaCancelamentoResponse resposta = service.cancelar(1L);

                verify(reservaRepository)
                                .save(reserva);

                assertEquals(
                                StatusReserva.CANCELADA,
                                reserva.getStatus());

                assertEquals(
                                "cancelada",
                                resposta.status());
        }

        @Test
        void filaNaoMostraReservasCanceladas() {

                Usuario usuario1 = new Usuario();
                usuario1.setNome("Leitor 1");

                Leitor leitor1 = mock(Leitor.class);
                when(leitor1.getId()).thenReturn(1L);
                when(leitor1.getUsuario()).thenReturn(usuario1);

                Reserva reserva1 = new Reserva();
                reserva1.setLeitor(leitor1);

                Usuario usuario2 = new Usuario();
                usuario2.setNome("Leitor 2");

                Leitor leitor2 = mock(Leitor.class);
                when(leitor2.getId()).thenReturn(2L);
                when(leitor2.getUsuario()).thenReturn(usuario2);

                Reserva reserva2 = new Reserva();
                reserva2.setLeitor(leitor2);

                when(reservaRepository.findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                1L,
                                StatusReserva.ATIVA))
                                .thenReturn(List.of(
                                                reserva1,
                                                reserva2));

                List<FilaReservaResponse> fila = service.listarFila(1L);

                assertEquals(2, fila.size());

                assertEquals(
                                "Leitor 1",
                                fila.get(0).leitorNome());

                assertEquals(
                                "Leitor 2",
                                fila.get(1).leitorNome());
        }
}