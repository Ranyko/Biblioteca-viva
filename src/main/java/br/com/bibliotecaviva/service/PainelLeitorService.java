package br.com.bibliotecaviva.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.PainelLeitorResponse;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.repository.LeitorRepository;
import br.com.bibliotecaviva.repository.PainelLeitorRepository;

@Service
public class PainelLeitorService {

    private final LeitorRepository leitorRepository;
    private final PainelLeitorRepository painelLeitorRepository;

    public PainelLeitorService(LeitorRepository leitorRepository, PainelLeitorRepository painelLeitorRepository) {
        this.leitorRepository = leitorRepository;
        this.painelLeitorRepository = painelLeitorRepository;
    }

    @Transactional(readOnly = true)
    public PainelLeitorResponse consultarPorUsuario(long usuarioId) {
        long leitorId = leitorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro de leitor não encontrado"))
                .getId();

        return new PainelLeitorResponse(
                painelLeitorRepository.listarEmprestimos(leitorId),
                painelLeitorRepository.listarReservas(leitorId),
                painelLeitorRepository.listarMultas(leitorId));
    }
}
