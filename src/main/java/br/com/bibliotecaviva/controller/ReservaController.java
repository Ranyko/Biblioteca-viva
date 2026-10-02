package br.com.bibliotecaviva.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.com.bibliotecaviva.dto.FilaReservaResponse;
import br.com.bibliotecaviva.dto.ReservaCreateRequest;
import br.com.bibliotecaviva.dto.ReservaResponse;
import br.com.bibliotecaviva.service.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/atendente/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> criar(
            @Valid @RequestBody ReservaCreateRequest request) {

        ReservaResponse criada = reservaService.criar(request);

        return ResponseEntity
                .created(URI.create("/atendente/reservas/" + criada.id()))
                .body(criada);
    }

    @GetMapping("/fila/{livroId}")
    public List<FilaReservaResponse> listarFila(
            @PathVariable Long livroId) {

        return reservaService.listarFila(livroId);
    }
}