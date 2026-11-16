package br.com.bibliotecaviva.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.bibliotecaviva.dto.PainelLeitorResponse;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.PainelLeitorService;

@RestController
@RequestMapping("/leitor")
public class LeitorController {

    private final PainelLeitorService painelLeitorService;

    public LeitorController(PainelLeitorService painelLeitorService) {
        this.painelLeitorService = painelLeitorService;
    }

    @GetMapping({"/painel", "/meu-painel"})
    public PainelLeitorResponse painel(@AuthenticationPrincipal Usuario usuario) {
        return painelLeitorService.consultarPorUsuario(usuario.getId());
    }
}
