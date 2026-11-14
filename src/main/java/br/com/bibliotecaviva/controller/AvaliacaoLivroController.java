package br.com.bibliotecaviva.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import br.com.bibliotecaviva.dto.AvaliacaoRequest;
import br.com.bibliotecaviva.dto.AvaliacaoResponse;
import br.com.bibliotecaviva.dto.AvaliacoesLivroResponse;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.AvaliacaoLivroService;

@RestController
public class AvaliacaoLivroController {
    private final AvaliacaoLivroService service;

    public AvaliacaoLivroController(AvaliacaoLivroService service) {
        this.service = service;
    }

    @PostMapping("/leitor/livros/{livroId}/avaliacao")
    public ResponseEntity<AvaliacaoResponse> criar(@PathVariable long livroId,
            @AuthenticationPrincipal Usuario usuario, @Valid @RequestBody AvaliacaoRequest request) {
        AvaliacaoResponse criada = service.criar(livroId, usuario.getId(), request);
        return ResponseEntity.created(URI.create("/leitor/livros/" + livroId + "/avaliacao")).body(criada);
    }

    @GetMapping("/leitor/livros/{livroId}/avaliacao")
    public AvaliacaoResponse buscarPropria(@PathVariable long livroId,
            @AuthenticationPrincipal Usuario usuario) {
        return service.buscarPropria(livroId, usuario.getId());
    }

    @PutMapping("/leitor/livros/{livroId}/avaliacao")
    public AvaliacaoResponse atualizar(@PathVariable long livroId,
            @AuthenticationPrincipal Usuario usuario, @Valid @RequestBody AvaliacaoRequest request) {
        return service.atualizar(livroId, usuario.getId(), request);
    }

    @DeleteMapping("/leitor/livros/{livroId}/avaliacao")
    public ResponseEntity<Void> excluir(@PathVariable long livroId, @AuthenticationPrincipal Usuario usuario) {
        service.excluir(livroId, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/acervo/livros/{livroId}/avaliacoes")
    public AvaliacoesLivroResponse listar(@PathVariable long livroId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(livroId, pagina, tamanho);
    }
}
