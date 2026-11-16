package br.com.bibliotecaviva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.model.Leitor;
import br.com.bibliotecaviva.repository.LeitorRepository;
import br.com.bibliotecaviva.repository.PainelLeitorRepository;

@ExtendWith(MockitoExtension.class)
class PainelLeitorServiceTest {

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private PainelLeitorRepository painelLeitorRepository;

    @InjectMocks
    private PainelLeitorService service;

    @Test
    void deveConsultarDadosDoLeitorAssociadoAoUsuarioAutenticado() {
        Leitor leitor = mock(Leitor.class);
        when(leitorRepository.findByUsuarioId(12L)).thenReturn(Optional.of(leitor));
        when(leitor.getId()).thenReturn(34L);
        when(painelLeitorRepository.listarEmprestimosAtivos(34L)).thenReturn(List.of());
        when(painelLeitorRepository.listarReservas(34L)).thenReturn(List.of());
        when(painelLeitorRepository.listarMultas(34L)).thenReturn(List.of());

        var resposta = service.consultarPorUsuario(12L);

        assertThat(resposta.emprestimos()).isEmpty();
        assertThat(resposta.reservas()).isEmpty();
        assertThat(resposta.multas()).isEmpty();
        verify(painelLeitorRepository).listarEmprestimosAtivos(34L);
        verify(painelLeitorRepository).listarReservas(34L);
        verify(painelLeitorRepository).listarMultas(34L);
    }

    @Test
    void deveRetornarNaoEncontradoQuandoUsuarioNaoPossuiCadastroDeLeitor() {
        when(leitorRepository.findByUsuarioId(12L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarPorUsuario(12L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cadastro de leitor não encontrado");
    }
}
