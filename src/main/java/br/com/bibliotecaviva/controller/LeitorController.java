package br.com.bibliotecaviva.controller;

import java.util.List;
import java.util.Map;
import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import br.com.bibliotecaviva.dto.PainelLeitorResponse;
import br.com.bibliotecaviva.dto.SugestaoLivroResponse;
import br.com.bibliotecaviva.model.Leitor;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.LeitorRepository;
import br.com.bibliotecaviva.service.PainelLeitorService;
import br.com.bibliotecaviva.service.SugestaoLivroService;
import br.com.bibliotecaviva.dto.ReservaLeitorRequest;
import br.com.bibliotecaviva.dto.ReservaResponse;
import br.com.bibliotecaviva.service.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/leitor")
public class LeitorController {

    private final LeitorRepository leitorRepository;
    private final SugestaoLivroService sugestaoLivroService;
    private final PainelLeitorService painelLeitorService;
    private final ReservaService reservaService;

    public LeitorController(
            LeitorRepository leitorRepository,
            SugestaoLivroService sugestaoLivroService,
            PainelLeitorService painelLeitorService,
            ReservaService reservaService) {
        this.leitorRepository = leitorRepository;
        this.sugestaoLivroService = sugestaoLivroService;
        this.painelLeitorService = painelLeitorService;
        this.reservaService = reservaService;
    }

    @GetMapping("/painel")
    public Map<String, String> painel() {
        return Map.of("mensagem", "Acesso de leitor autorizado");
    }

    @GetMapping("/meu-painel")
    public PainelLeitorResponse consultarMeuPainel(
            @AuthenticationPrincipal Usuario usuario) {

        Leitor leitor = leitorRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cadastro de leitor não encontrado."));

        return painelLeitorService.consultar(leitor.getId());
    }

    @GetMapping("/sugestoes")
    public List<SugestaoLivroResponse> buscarSugestoes(
            @AuthenticationPrincipal Usuario usuario) {

        Leitor leitor = leitorRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cadastro de leitor não encontrado."));

        return sugestaoLivroService.buscarSugestoes(leitor.getId());
    }

    @PostMapping("/reservas")
    public ResponseEntity<ReservaResponse> criarReserva(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ReservaLeitorRequest request) {

        Leitor leitor = leitorRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cadastro de leitor não encontrado."));

        ReservaResponse criada = reservaService.criarParaLeitor(
                leitor,
                request.livroId());

        return ResponseEntity
                .created(URI.create("/leitor/reservas/" + criada.id()))
                .body(criada);
    }
}
