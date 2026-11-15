package br.com.bibliotecaviva.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.VarreduraResponse;
import br.com.bibliotecaviva.model.Aviso.ReferenciaTipo;
import br.com.bibliotecaviva.model.Aviso.TipoAviso;
import br.com.bibliotecaviva.repository.AvisoRepository;

@Service
public class AvisoVarreduraService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AvisoRepository repository;
    private final AvisoService avisoService;
    private final Clock clock;

    public AvisoVarreduraService(AvisoRepository repository, AvisoService avisoService, Clock clock) {
        this.repository = repository;
        this.avisoService = avisoService;
        this.clock = clock;
    }

    @Transactional
    public VarreduraResponse executar() {
        LocalDate hoje = LocalDate.now(clock);

        int devidaAmanha = varrerDevidaAmanha(hoje);
        int vencido = varrerVencidos(hoje);
        int reservaExpirando = varrerReservasExpirando(hoje);
        int reservaExpirada = varrerReservasExpiradas(hoje);
        int filaPosicao = varrerFila(hoje);

        int total = devidaAmanha + vencido + reservaExpirando + reservaExpirada + filaPosicao;
        return new VarreduraResponse(devidaAmanha, vencido, reservaExpirando, reservaExpirada, filaPosicao, total);
    }

    private int varrerDevidaAmanha(LocalDate hoje) {
        LocalDate amanha = hoje.plusDays(1);
        int criados = 0;
        for (var alvo : repository.emprestimosComPrevista(amanha)) {
            Optional<Long> id = avisoService.criar(alvo.usuarioId(), TipoAviso.DEVIDA_AMANHA,
                    "Devolução vence amanhã",
                    "Seu empréstimo vence em " + amanha.format(DATA) + ". Devolva para evitar multa.",
                    ReferenciaTipo.emprestimo, alvo.emprestimoId(), hoje);
            if (id.isPresent()) criados++;
        }
        return criados;
    }

    private int varrerVencidos(LocalDate hoje) {
        int criados = 0;
        for (var alvo : repository.emprestimosAtrasados(hoje)) {
            Optional<Long> id = avisoService.criar(alvo.usuarioId(), TipoAviso.VENCIDO,
                    "Empréstimo em atraso",
                    "Seu empréstimo venceu em " + alvo.dataPrevista().format(DATA) + " e segue em atraso.",
                    ReferenciaTipo.emprestimo, alvo.emprestimoId(), hoje);
            if (id.isPresent()) criados++;
        }
        return criados;
    }

    private int varrerReservasExpirando(LocalDate hoje) {
        LocalDate amanha = hoje.plusDays(1);
        int criados = 0;
        for (var alvo : repository.reservasDisponiveisComLimite(amanha)) {
            Optional<Long> id = avisoService.criar(alvo.usuarioId(), TipoAviso.RESERVA_EXPIRANDO,
                    "Reserva expira amanhã",
                    "Retire o exemplar reservado até " + amanha.format(DATA) + " ou a reserva expira.",
                    ReferenciaTipo.reserva, alvo.reservaId(), hoje);
            if (id.isPresent()) criados++;
        }
        return criados;
    }

    private int varrerReservasExpiradas(LocalDate hoje) {
        int criados = 0;
        // Decisão A: apenas detecta reservas JÁ expiradas no banco; não escreve estado de negócio.
        // `diaReferencia = null` → 1 aviso por reserva, para sempre (o índice NULLS NOT DISTINCT deduplica).
        // Sem isso, uma reserva expirada reemitiria todo dia indefinidamente, pois não há evento que
        // mude o status depois — diferente do VENCIDO, cujo lembrete diário é deliberado (§8).
        for (var alvo : repository.reservasExpiradas()) {
            Optional<Long> id = avisoService.criar(alvo.usuarioId(), TipoAviso.RESERVA_EXPIRADA,
                    "Reserva expirada",
                    "Sua reserva expirou por não retirada no prazo.",
                    ReferenciaTipo.reserva, alvo.reservaId(), null);
            if (id.isPresent()) criados++;
        }
        return criados;
    }

    private int varrerFila(LocalDate hoje) {
        int criados = 0;
        for (var alvo : repository.posicoesDaFila()) {
            Integer ultima = alvo.ultimaObservada();
            if (ultima == null) {
                // Baseline: grava a posição observada sem avisar (evita notificar a entrada na fila).
                repository.gravarUltimaPosicao(alvo.reservaId(), alvo.posicao());
                continue;
            }
            if (alvo.posicao() < ultima) {
                Optional<Long> id = avisoService.criar(alvo.usuarioId(), TipoAviso.FILA_POSICAO,
                        "Sua posição na fila avançou",
                        "Você agora é o " + alvo.posicao() + "º da fila de reserva.",
                        ReferenciaTipo.reserva, alvo.reservaId(), hoje);
                if (id.isPresent()) criados++;
            }
            // Auto-atualiza a baseline: p == baseline na próxima varredura não emite de novo.
            repository.gravarUltimaPosicao(alvo.reservaId(), alvo.posicao());
        }
        return criados;
    }
}