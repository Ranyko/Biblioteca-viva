package br.com.bibliotecaviva.service;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.bibliotecaviva.dto.ClubeLeituraRequest;
import br.com.bibliotecaviva.dto.ClubeLeituraResponse;
import br.com.bibliotecaviva.dto.EncontroClubeRequest;
import br.com.bibliotecaviva.dto.EncontroClubeResponse;
import br.com.bibliotecaviva.dto.InscricaoEncontroResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.repository.ClubeLeituraRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubeLeituraServiceTest {
    @Mock ClubeLeituraRepository repository;
    ClubeLeituraService service;

    private static final OffsetDateTime AGORA = OffsetDateTime.parse("2026-10-01T10:00:00-03:00");
    private static final OffsetDateTime FUTURO = OffsetDateTime.parse("2026-10-02T10:00:00-03:00");

    @BeforeEach
    void preparar() {
        service = new ClubeLeituraService(repository,
            Clock.fixed(Instant.parse("2026-10-01T13:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    private ClubeLeituraRepository.ClubeDados clube(String status, int capacidade) {
        return new ClubeLeituraRepository.ClubeDados(3, "Leitura", null, status, capacidade,
            "mensal", 1, AGORA);
    }

    private ClubeLeituraRepository.EncontroDados encontro(String status, OffsetDateTime dataHora) {
        return new ClubeLeituraRepository.EncontroDados(5, 3, 7, dataHora, status);
    }

    private ClubeLeituraRequest pedidoClube(int capacidade, List<Long> categorias) {
        return new ClubeLeituraRequest(" Leitura ", null, capacidade, " mensal ", categorias);
    }

    @Test
    void criaClubeComCategoriasEResponsavelAutenticado() {
        when(repository.criarClube("Leitura", null, 8, "mensal", 1)).thenReturn(3L);
        ClubeLeituraResponse esperado = new ClubeLeituraResponse(3, "Leitura", null, "ativo", 8,
            "mensal", 1, AGORA, List.of());
        when(repository.clube(3, false)).thenReturn(esperado);

        assertSame(esperado, service.criarClube(pedidoClube(8, List.of(2L, 4L)), 1));

        verify(repository).conferirCategorias(List.of(2L, 4L));
        verify(repository).substituirCategorias(3, List.of(2L, 4L));
    }

    @Test
    void rejeitaCapacidadeInvalidaSemGravar() {
        assertThrows(IllegalArgumentException.class,
            () -> service.criarClube(pedidoClube(0, List.of()), 1));
        verifyNoInteractions(repository);
    }

    @Test
    void naoReduzCapacidadeAbaixoDasInscricoesConfirmadas() {
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("ativo", 5));
        when(repository.maiorLotacaoEmEncontrosAbertos(3)).thenReturn(4L);

        assertThrows(ConflitoException.class,
            () -> service.atualizarClube(3, pedidoClube(3, List.of())));
        verify(repository, never()).atualizarClube(anyLong(), anyString(), any(), anyInt(), any());
    }

    @Test
    void encontroRequerClubeAtivoLivroExistenteEDataFutura() {
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("encerrado", 8));
        assertThrows(ConflitoException.class,
            () -> service.criarEncontro(3, new EncontroClubeRequest(7L, FUTURO, "Sala 1")));
        verify(repository, never()).livroExiste(anyLong());
    }

    @Test
    void criaEncontroParaLivroExistente() {
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("ativo", 8));
        when(repository.livroExiste(7)).thenReturn(true);
        EncontroClubeRequest request = new EncontroClubeRequest(7L, FUTURO, " Sala 1 ");
        EncontroClubeResponse esperado = new EncontroClubeResponse(5, 3, "Leitura", 7, "Livro",
            FUTURO, "Sala 1", "aberto", 8, 0, 8, AGORA);
        when(repository.criarEncontro(3, 7, FUTURO, "Sala 1")).thenReturn(esperado);

        assertSame(esperado, service.criarEncontro(3, request));
    }

    @Test
    void leitorAtivoSeInscreveEInscricaoUsaCapacidadeDoClube() {
        when(repository.encontroDados(5)).thenReturn(encontro("aberto", FUTURO));
        when(repository.travarLeitorDoUsuario(2)).thenReturn(
            new ClubeLeituraRepository.LeitorDados(9, true, "leitor"));
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("ativo", 1));
        when(repository.travarEncontro(3, 5)).thenReturn(encontro("aberto", FUTURO));
        when(repository.inscricao(5, 9)).thenReturn(Optional.empty());
        when(repository.contarInscricoesConfirmadas(5)).thenReturn(0L);
        InscricaoEncontroResponse esperado = new InscricaoEncontroResponse(
            1, 5, 9, "confirmada", AGORA, null, null, null);
        when(repository.criarInscricao(5, 9)).thenReturn(esperado);

        assertSame(esperado, service.inscrever(5, 2));
    }

    @Test
    void recusaInscricaoDuplicadaMesmoSeAnteriorFoiCancelada() {
        when(repository.encontroDados(5)).thenReturn(encontro("aberto", FUTURO));
        when(repository.travarLeitorDoUsuario(2)).thenReturn(
            new ClubeLeituraRepository.LeitorDados(9, true, "leitor"));
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("ativo", 8));
        when(repository.travarEncontro(3, 5)).thenReturn(encontro("aberto", FUTURO));
        when(repository.inscricao(5, 9)).thenReturn(Optional.of(
            new InscricaoEncontroResponse(1, 5, 9, "cancelada", AGORA, AGORA, null, null)));

        assertThrows(ConflitoException.class, () -> service.inscrever(5, 2));
        verify(repository, never()).criarInscricao(anyLong(), anyLong());
    }

    @Test
    void recusaInscricaoDeLeitorInativo() {
        when(repository.encontroDados(5)).thenReturn(encontro("aberto", FUTURO));
        when(repository.travarLeitorDoUsuario(2)).thenReturn(
            new ClubeLeituraRepository.LeitorDados(9, false, "leitor"));

        assertThrows(ConflitoException.class, () -> service.inscrever(5, 2));
        verify(repository, never()).buscarClubeDados(anyLong(), anyBoolean());
    }

    @Test
    void recusaEncontroLotado() {
        when(repository.encontroDados(5)).thenReturn(encontro("aberto", FUTURO));
        when(repository.travarLeitorDoUsuario(2)).thenReturn(
            new ClubeLeituraRepository.LeitorDados(9, true, "leitor"));
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("ativo", 1));
        when(repository.travarEncontro(3, 5)).thenReturn(encontro("aberto", FUTURO));
        when(repository.inscricao(5, 9)).thenReturn(Optional.empty());
        when(repository.contarInscricoesConfirmadas(5)).thenReturn(1L);

        assertThrows(ConflitoException.class, () -> service.inscrever(5, 2));
        verify(repository, never()).criarInscricao(anyLong(), anyLong());
    }

    @Test
    void recusaPresencaAntesDoEncontro() {
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("ativo", 8));
        when(repository.travarEncontro(3, 5)).thenReturn(encontro("aberto", FUTURO));
        assertThrows(ConflitoException.class, () -> service.registrarPresenca(3, 5, 9, true));
        verify(repository, never()).registrarPresenca(anyLong(), anyLong(), anyBoolean(), any());
    }

    @Test
    void naoConcluiEncontroComPresencasPendentes() {
        OffsetDateTime passado = AGORA.minusDays(1);
        when(repository.buscarClubeDados(3, true)).thenReturn(clube("ativo", 8));
        when(repository.travarEncontro(3, 5)).thenReturn(encontro("aberto", passado));
        when(repository.existemPresencasPendentes(5)).thenReturn(true);
        assertThrows(ConflitoException.class, () -> service.concluirEncontro(3, 5));
        verify(repository, never()).alterarStatusEncontro(anyLong(), anyLong(), anyString());
    }

    @Test
    void idsInvalidosNaoAcessamRepositorio() {
        assertThrows(IllegalArgumentException.class, () -> service.buscarClube(0));
        assertThrows(IllegalArgumentException.class, () -> service.listarClubes(false, null, null, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listarParticipantes(1, 2, 0, 101));
        verifyNoInteractions(repository);
    }
}
