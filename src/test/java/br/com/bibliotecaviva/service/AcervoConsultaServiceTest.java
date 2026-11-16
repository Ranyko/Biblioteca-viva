package br.com.bibliotecaviva.service;

import org.junit.jupiter.api.Test;
import br.com.bibliotecaviva.repository.AcervoConsultaRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AcervoConsultaServiceTest {
    private final AcervoConsultaRepository repository = mock(AcervoConsultaRepository.class);
    private final AcervoConsultaService service = new AcervoConsultaService(repository);

    @Test void normalizaFiltros() {
        service.pesquisar(" Dom ", null, "   ", 0, 20);
        verify(repository).pesquisar("Dom", "", "", 0, 20);
    }
    @Test void rejeitaPaginaNegativa() { assertThrows(IllegalArgumentException.class, () -> service.pesquisar("", "", "", -1, 20)); }
    @Test void rejeitaTamanhoZero() { assertThrows(IllegalArgumentException.class, () -> service.pesquisar("", "", "", 0, 0)); }
    @Test void limitaTamanho() { assertThrows(IllegalArgumentException.class, () -> service.pesquisar("", "", "", 0, 101)); }
    @Test void limitaFiltro() { assertThrows(IllegalArgumentException.class, () -> service.pesquisar("x".repeat(201), "", "", 0, 20)); }
}
