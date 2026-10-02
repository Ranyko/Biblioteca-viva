package br.com.bibliotecaviva.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.CirculacaoService;

@RestController
@RequestMapping("/atendente/emprestimos")
public class CirculacaoController {
    private final CirculacaoService service;
    public CirculacaoController(CirculacaoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<EmprestimoResponse> emprestar(@Valid @RequestBody EmprestimoCreateRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        var criado = service.emprestar(request, usuario.getId());
        return ResponseEntity.created(URI.create("/atendente/emprestimos/" + criado.id())).body(criado);
    }

    @PostMapping("/{id}/devolucao")
    public DevolucaoResponse devolver(@PathVariable long id, @AuthenticationPrincipal Usuario usuario) {
        return service.devolver(id, usuario.getId());
    }

    @GetMapping("/{id}")
    public EmprestimoResponse buscar(@PathVariable long id) { return service.buscar(id); }

    @GetMapping
    public List<EmprestimoResponse> listar(@RequestParam(defaultValue = "true") boolean ativos,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(ativos, pagina, tamanho);
    }
}
