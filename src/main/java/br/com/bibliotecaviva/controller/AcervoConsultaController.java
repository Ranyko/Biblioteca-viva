package br.com.bibliotecaviva.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.bibliotecaviva.dto.AcervoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.service.AcervoConsultaService;

@RestController
public class AcervoConsultaController {
    private final AcervoConsultaService service;
    public AcervoConsultaController(AcervoConsultaService service) { this.service = service; }

    @GetMapping("/acervo")
    public PaginaResponse<AcervoResponse> pesquisar(@RequestParam(defaultValue = "") String titulo,
            @RequestParam(defaultValue = "") String autor, @RequestParam(defaultValue = "") String categoria,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return service.pesquisar(titulo, autor, categoria, pagina, tamanho);
    }
}
