package br.com.bibliotecaviva.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.PainelBibliotecaResponse;
import br.com.bibliotecaviva.repository.PainelBibliotecaRepository;

@Service
public class PainelBibliotecaService {

    private final PainelBibliotecaRepository repository;

    public PainelBibliotecaService(PainelBibliotecaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PainelBibliotecaResponse consultar() {
        return new PainelBibliotecaResponse(
                repository.contarEmprestimosAtivos(),
                repository.contarEmprestimosAtrasados(),
                repository.buscarRankingObras(),
                repository.buscarEmprestimosPorCategoria());
    }
}