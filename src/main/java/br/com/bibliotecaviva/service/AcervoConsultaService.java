package br.com.bibliotecaviva.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import br.com.bibliotecaviva.dto.AcervoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.repository.AcervoConsultaRepository;

@Service
public class AcervoConsultaService {
    private final AcervoConsultaRepository repository;
    public AcervoConsultaService(AcervoConsultaRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PaginaResponse<AcervoResponse> pesquisar(String titulo, String autor, String categoria, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Página deve ser >= 0 e tamanho deve estar entre 1 e 100");
        }
        return repository.pesquisar(filtro(titulo), filtro(autor), filtro(categoria), pagina, tamanho);
    }

    private String filtro(String valor) {
        String normalizado = valor == null ? "" : valor.trim();
        if (normalizado.length() > 200) throw new IllegalArgumentException("Filtro deve ter no máximo 200 caracteres");
        return normalizado;
    }
}
