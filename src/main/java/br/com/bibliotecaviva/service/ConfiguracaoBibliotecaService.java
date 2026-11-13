package br.com.bibliotecaviva.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.bibliotecaviva.dto.ConfiguracaoBibliotecaRequest;
import br.com.bibliotecaviva.dto.ConfiguracaoBibliotecaResponse;
import br.com.bibliotecaviva.repository.ConfiguracaoBibliotecaRepository;

@Service
public class ConfiguracaoBibliotecaService {
    private final ConfiguracaoBibliotecaRepository repository;
    private final Clock clock;

    public ConfiguracaoBibliotecaService(ConfiguracaoBibliotecaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoBibliotecaResponse buscar() { return repository.buscar(); }

    @Transactional
    public ConfiguracaoBibliotecaResponse atualizar(ConfiguracaoBibliotecaRequest request) {
        return repository.atualizar(request, OffsetDateTime.now(clock));
    }
}
