package br.com.bibliotecaviva.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.bibliotecaviva.dto.ClubeLeituraResponse;
import br.com.bibliotecaviva.dto.EncontroClubeRequest;
import br.com.bibliotecaviva.dto.EncontroClubeResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.dto.ParticipanteEncontroResponse;
import br.com.bibliotecaviva.dto.PresencaEncontroRequest;
import br.com.bibliotecaviva.service.ClubeLeituraService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/atendente/clubes-leitura")
public class EncontroClubeController {
    private final ClubeLeituraService service;

    public EncontroClubeController(ClubeLeituraService service) {
        this.service = service;
    }

    @GetMapping
    public PaginaResponse<ClubeLeituraResponse> clubes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listarClubes(true, nome, categoriaId, pagina, tamanho);
    }

    @GetMapping("/{clubeId}/encontros")
    public PaginaResponse<EncontroClubeResponse> encontros(@PathVariable long clubeId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listarEncontros(clubeId, false, pagina, tamanho);
    }

    @PostMapping("/{clubeId}/encontros")
    public ResponseEntity<EncontroClubeResponse> criar(@PathVariable long clubeId,
            @Valid @RequestBody EncontroClubeRequest request) {
        EncontroClubeResponse criado = service.criarEncontro(clubeId, request);
        return ResponseEntity.created(URI.create("/atendente/clubes-leitura/" + clubeId
            + "/encontros/" + criado.id())).body(criado);
    }

    @PutMapping("/{clubeId}/encontros/{encontroId}")
    public EncontroClubeResponse atualizar(@PathVariable long clubeId, @PathVariable long encontroId,
            @Valid @RequestBody EncontroClubeRequest request) {
        return service.atualizarEncontro(clubeId, encontroId, request);
    }

    @PatchMapping("/{clubeId}/encontros/{encontroId}/cancelar")
    public EncontroClubeResponse cancelar(@PathVariable long clubeId, @PathVariable long encontroId) {
        return service.cancelarEncontro(clubeId, encontroId);
    }

    @GetMapping("/{clubeId}/encontros/{encontroId}/inscricoes")
    public PaginaResponse<ParticipanteEncontroResponse> participantes(@PathVariable long clubeId,
            @PathVariable long encontroId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listarParticipantes(clubeId, encontroId, pagina, tamanho);
    }

    @PatchMapping("/{clubeId}/encontros/{encontroId}/inscricoes/{leitorId}/presenca")
    public ParticipanteEncontroResponse presenca(@PathVariable long clubeId, @PathVariable long encontroId,
            @PathVariable long leitorId, @Valid @RequestBody PresencaEncontroRequest request) {
        return service.registrarPresenca(clubeId, encontroId, leitorId, request.presente());
    }

    @PatchMapping("/{clubeId}/encontros/{encontroId}/concluir")
    public EncontroClubeResponse concluir(@PathVariable long clubeId, @PathVariable long encontroId) {
        return service.concluirEncontro(clubeId, encontroId);
    }
}
