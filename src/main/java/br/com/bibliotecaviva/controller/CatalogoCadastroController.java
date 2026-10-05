package br.com.bibliotecaviva.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.repository.CatalogoCadastroRepository.Tipo;
import br.com.bibliotecaviva.service.CatalogoCadastroService;

@RestController
@RequestMapping("/admin")
public class CatalogoCadastroController {
    private final CatalogoCadastroService service;
    public CatalogoCadastroController(CatalogoCadastroService service) { this.service=service; }
    @GetMapping("/autores")
    public List<ReferenciaResponse> autores(@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanho) {
        return service.listarReferencias(Tipo.AUTOR,pagina,tamanho);
    }
    @PostMapping("/autores")
    public ResponseEntity<ReferenciaResponse> autor(@Valid @RequestBody NomeCadastroRequest r) {
        var criado=service.criarReferencia(Tipo.AUTOR,r);
        return ResponseEntity.created(URI.create("/admin/autores/"+criado.id())).body(criado);
    }
    @PutMapping("/autores/{id}")
    public ReferenciaResponse autor(@PathVariable long id,@Valid @RequestBody NomeCadastroRequest r) {
        return service.atualizarReferencia(Tipo.AUTOR,id,r);
    }
    @GetMapping("/autores/{id}")
    public ReferenciaResponse autor(@PathVariable long id) { return service.buscarReferencia(Tipo.AUTOR,id); }
    @GetMapping("/categorias")
    public List<ReferenciaResponse> categorias(@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanho) {
        return service.listarReferencias(Tipo.CATEGORIA,pagina,tamanho);
    }
    @PostMapping("/categorias")
    public ResponseEntity<ReferenciaResponse> categoria(@Valid @RequestBody NomeCadastroRequest r) {
        var criado=service.criarReferencia(Tipo.CATEGORIA,r);
        return ResponseEntity.created(URI.create("/admin/categorias/"+criado.id())).body(criado);
    }
    @PutMapping("/categorias/{id}")
    public ReferenciaResponse categoria(@PathVariable long id,@Valid @RequestBody NomeCadastroRequest r) {
        return service.atualizarReferencia(Tipo.CATEGORIA,id,r);
    }
    @GetMapping("/categorias/{id}")
    public ReferenciaResponse categoria(@PathVariable long id) { return service.buscarReferencia(Tipo.CATEGORIA,id); }
    @GetMapping("/livros")
    public PaginaResponse<LivroResponse> livros(@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanho) {
        return service.listarLivros(pagina,tamanho);
    }
    @GetMapping("/livros/{id}")
    public LivroResponse livro(@PathVariable long id) { return service.buscarLivro(id); }
    @PostMapping("/livros")
    public ResponseEntity<LivroResponse> livro(@Valid @RequestBody LivroRequest r) {
        var criado=service.criarLivro(r);
        return ResponseEntity.created(URI.create("/admin/livros/"+criado.id())).body(criado);
    }
    @PutMapping("/livros/{id}")
    public LivroResponse livro(@PathVariable long id,@Valid @RequestBody LivroRequest r) { return service.atualizarLivro(id,r); }
    @GetMapping("/exemplares")
    public PaginaResponse<ExemplarResponse> exemplares(@RequestParam(required=false) Long livroId,
            @RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanho) {
        return service.listarExemplares(livroId,pagina,tamanho);
    }
    @GetMapping("/exemplares/{id}")
    public ExemplarResponse exemplar(@PathVariable long id) { return service.buscarExemplar(id); }
    @PostMapping("/exemplares")
    public ResponseEntity<ExemplarResponse> exemplar(@Valid @RequestBody ExemplarCreateRequest r) {
        var criado=service.criarExemplar(r);
        return ResponseEntity.created(URI.create("/admin/exemplares/"+criado.id())).body(criado);
    }
    @PutMapping("/exemplares/{id}")
    public ExemplarResponse exemplar(@PathVariable long id,@Valid @RequestBody ExemplarUpdateRequest r) {
        return service.atualizarExemplar(id,r);
    }
    @PatchMapping("/exemplares/{id}/situacao")
    public ExemplarResponse situacao(@PathVariable long id,@Valid @RequestBody ExemplarSituacaoRequest r) {
        return service.situacaoExemplar(id,r.ativo());
    }
}
