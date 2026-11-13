package br.com.bibliotecaviva.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.bibliotecaviva.dto.MultaResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.service.MultaService;

@RestController
@RequestMapping("/atendente/multas")
public class MultaController {
    private final MultaService service;
    public MultaController(MultaService service) { this.service = service; }

    @GetMapping
    public PaginaResponse<MultaResponse> listar(@RequestParam(required = false) String status,
            @RequestParam(required = false) Long leitorId, @RequestParam(required = false) Long emprestimoId,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(status, leitorId, emprestimoId, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public MultaResponse buscar(@PathVariable long id) { return service.buscar(id); }

    @PostMapping("/{id}/baixa")
    public MultaResponse baixar(@PathVariable long id) { return service.baixar(id); }
}
