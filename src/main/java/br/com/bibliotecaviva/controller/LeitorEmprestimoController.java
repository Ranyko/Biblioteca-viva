package br.com.bibliotecaviva.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.bibliotecaviva.dto.RenovacaoResponse;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.CirculacaoService;

@RestController
@RequestMapping("/leitor/emprestimos")
public class LeitorEmprestimoController {
    private final CirculacaoService service;

    public LeitorEmprestimoController(CirculacaoService service) {
        this.service = service;
    }

    @PostMapping("/{id}/renovacao")
    public RenovacaoResponse renovar(@PathVariable long id, @AuthenticationPrincipal Usuario usuario) {
        return service.renovar(id, usuario.getId());
    }

    @GetMapping("/{id}/renovacoes")
    public List<RenovacaoResponse> historico(@PathVariable long id, @AuthenticationPrincipal Usuario usuario) {
        return service.historicoRenovacoes(id, usuario.getId());
    }
}
