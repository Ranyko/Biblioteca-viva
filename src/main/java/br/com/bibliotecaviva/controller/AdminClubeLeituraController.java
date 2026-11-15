package br.com.bibliotecaviva.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.bibliotecaviva.dto.ClubeLeituraRequest;
import br.com.bibliotecaviva.dto.ClubeLeituraResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.dto.StatusClubeLeituraRequest;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.ClubeLeituraService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/clubes-leitura")
public class AdminClubeLeituraController {
    private final ClubeLeituraService service;

    public AdminClubeLeituraController(ClubeLeituraService service) {
        this.service = service;
    }

    @GetMapping
    public PaginaResponse<ClubeLeituraResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listarClubes(true, nome, categoriaId, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public ClubeLeituraResponse buscar(@PathVariable long id) {
        return service.buscarClube(id);
    }

    @PostMapping
    public ResponseEntity<ClubeLeituraResponse> criar(@Valid @RequestBody ClubeLeituraRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        ClubeLeituraResponse criado = service.criarClube(request, usuario.getId());
        return ResponseEntity.created(URI.create("/admin/clubes-leitura/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ClubeLeituraResponse atualizar(@PathVariable long id,
            @Valid @RequestBody ClubeLeituraRequest request) {
        return service.atualizarClube(id, request);
    }

    @PatchMapping("/{id}/status")
    public ClubeLeituraResponse status(@PathVariable long id,
            @Valid @RequestBody StatusClubeLeituraRequest request) {
        return service.alterarStatusClube(id, request.status());
    }
}
