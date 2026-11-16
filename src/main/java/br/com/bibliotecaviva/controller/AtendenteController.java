package br.com.bibliotecaviva.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.bibliotecaviva.dto.LeitorCreateRequest;
import br.com.bibliotecaviva.dto.LeitorResponse;
import br.com.bibliotecaviva.service.LeitorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/atendente")
public class AtendenteController {

    private final LeitorService leitorService;

    public AtendenteController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @GetMapping("/painel")
    public Map<String, String> painel() {
        return Map.of("mensagem", "Acesso de atendente autorizado");
    }

    @GetMapping("/leitores")
    public List<LeitorResponse> listarLeitores() {
        return leitorService.listar();
    }

    @PostMapping("/leitores")
    public ResponseEntity<LeitorResponse> criarLeitor(@Valid @RequestBody LeitorCreateRequest request) {
        LeitorResponse criado = leitorService.criar(request);
        return ResponseEntity.created(URI.create("/atendente/leitores/" + criado.id())).body(criado);
    }
}
