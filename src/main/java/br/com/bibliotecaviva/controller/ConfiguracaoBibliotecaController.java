package br.com.bibliotecaviva.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.bibliotecaviva.dto.ConfiguracaoBibliotecaRequest;
import br.com.bibliotecaviva.dto.ConfiguracaoBibliotecaResponse;
import br.com.bibliotecaviva.service.ConfiguracaoBibliotecaService;

@RestController
@RequestMapping("/admin/configuracao")
public class ConfiguracaoBibliotecaController {
    private final ConfiguracaoBibliotecaService service;
    public ConfiguracaoBibliotecaController(ConfiguracaoBibliotecaService service) { this.service = service; }

    @GetMapping
    public ConfiguracaoBibliotecaResponse buscar() { return service.buscar(); }

    @PutMapping
    public ConfiguracaoBibliotecaResponse atualizar(@Valid @RequestBody ConfiguracaoBibliotecaRequest request) {
        return service.atualizar(request);
    }
}
