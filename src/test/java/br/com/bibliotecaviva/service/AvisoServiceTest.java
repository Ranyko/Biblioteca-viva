package br.com.bibliotecaviva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.bibliotecaviva.dto.AvisoPreferencias;
import br.com.bibliotecaviva.dto.AvisoResponse;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.model.Aviso.ReferenciaTipo;
import br.com.bibliotecaviva.model.Aviso.TipoAviso;
import br.com.bibliotecaviva.model.PerfilUsuario;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.AvisoRepository;
import br.com.bibliotecaviva.repository.UsuarioRepository;

class AvisoServiceTest {

    private final AvisoRepository repository = mock(AvisoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-01T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private final AvisoService service = new AvisoService(repository, usuarios, clock);

    private Usuario usuario(boolean ativo) {
        Usuario u = new Usuario();
        u.setPerfil(PerfilUsuario.LEITOR);
        u.setAtivo(ativo);
        return u;
    }

    private AvisoPreferencias todasLigadas() {
        return new AvisoPreferencias(true, true, true, true, true, true, true, OffsetDateTime.now(clock));
    }

    @BeforeEach void preparar() {
        when(repository.preferencias()).thenReturn(todasLigadas());
        when(usuarios.findById(50L)).thenReturn(Optional.of(usuario(true)));
        when(repository.inserirSeAusente(any(), anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(Optional.of(1L));
    }

    @Test void criaAvisoQuandoTipoHabilitadoEUsuarioAtivo() {
        Optional<Long> id = service.criar(50L, TipoAviso.VENCIDO, "t", "m", ReferenciaTipo.emprestimo, 10L,
                LocalDate.of(2026, 10, 1));
        assertTrue(id.isPresent());
        verify(repository).inserirSeAusente(eq(50L), eq("VENCIDO"), eq("t"), eq("m"),
                eq("emprestimo"), eq(10L), eq(LocalDate.of(2026, 10, 1)), eq(OffsetDateTime.now(clock)));
    }

    @Test void naoCriaQuandoTipoDesligado() {
        when(repository.preferencias()).thenReturn(
                new AvisoPreferencias(true, true, true, true, true, true, false, OffsetDateTime.now(clock)));
        assertFalse(service.criar(50L, TipoAviso.FILA_POSICAO, "t", "m", ReferenciaTipo.reserva, 7L,
                LocalDate.of(2026, 10, 1)).isPresent());
        verify(repository, never()).inserirSeAusente(any(), anyString(), anyString(), anyString(), any(), any(), any(), any());
    }

    @Test void naoCriaParaUsuarioInativo() {
        when(usuarios.findById(50L)).thenReturn(Optional.of(usuario(false)));
        assertFalse(service.criar(50L, TipoAviso.VENCIDO, "t", "m", ReferenciaTipo.emprestimo, 10L, null).isPresent());
        verify(repository, never()).inserirSeAusente(any(), anyString(), anyString(), anyString(), any(), any(), any(), any());
    }

    @Test void paginacaoInvalidaLancaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.listar(50L, null, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listar(50L, null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> service.listar(50L, null, 0, 101));
    }

    @Test void marcarAvisoDeOutroUsuarioLancaNaoEncontrado() {
        // UPDATE filtrado por usuario_id não afeta linha; buscar também não encontra → 404.
        when(repository.buscar(99L, 50L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.marcarLido(99L, 50L));
    }

    @Test void marcarAvisoLidoDevolveEstadoAtual() {
        AvisoResponse lido = new AvisoResponse(1L, "VENCIDO", "t", "m", "emprestimo", 10L, null,
                OffsetDateTime.now(clock), OffsetDateTime.now(clock));
        when(repository.buscar(1L, 50L)).thenReturn(Optional.of(lido));
        assertEquals(lido, service.marcarLido(1L, 50L));
    }
}