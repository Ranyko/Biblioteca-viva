package br.com.bibliotecaviva.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.bibliotecaviva.dto.VarreduraResponse;
import br.com.bibliotecaviva.service.AvisoVarreduraService;

@Component
@ConditionalOnProperty("app.avisos.varredura-habilitada")
public class AvisoVarreduraAgendador {

    private static final Logger log = LoggerFactory.getLogger(AvisoVarreduraAgendador.class);

    private final AvisoVarreduraService varreduraService;

    public AvisoVarreduraAgendador(AvisoVarreduraService varreduraService) {
        this.varreduraService = varreduraService;
    }

    @Scheduled(cron = "${app.avisos.varredura-cron:0 0 6 * * *}",
            zone = "${app.biblioteca.zone-id:America/Sao_Paulo}")
    public void executar() {
        log.info("Varredura de avisos: início");
        VarreduraResponse r = varreduraService.executar();
        log.info("Varredura de avisos: fim — devidaAmanha={} vencido={} reservaExpirando={} reservaExpirada={} filaPosicao={} total={}",
                r.devidaAmanha(), r.vencido(), r.reservaExpirando(), r.reservaExpirada(), r.filaPosicao(), r.total());
    }
}