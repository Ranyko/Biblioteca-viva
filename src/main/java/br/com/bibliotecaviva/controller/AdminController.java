package br.com.bibliotecaviva.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.bibliotecaviva.dto.PainelBibliotecaResponse;
import br.com.bibliotecaviva.dto.UsuarioCreateRequest;
import br.com.bibliotecaviva.dto.UsuarioResponse;
import br.com.bibliotecaviva.dto.UsuarioUpdateRequest;
import br.com.bibliotecaviva.service.PainelBibliotecaService;
import br.com.bibliotecaviva.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final PainelBibliotecaService painelBibliotecaService;

    public AdminController(
            UsuarioService usuarioService,
            PainelBibliotecaService painelBibliotecaService) {
        this.usuarioService = usuarioService;
        this.painelBibliotecaService = painelBibliotecaService;
    }

    @GetMapping("/painel")
    public PainelBibliotecaResponse painel() {
        return painelBibliotecaService.consultar();
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listar();
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> criarUsuario(
            @Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse criado = usuarioService.criar(request);

        return ResponseEntity
                .created(URI.create("/admin/usuarios/" + criado.id()))
                .body(criado);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioResponse atualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.atualizar(id, request);
    }
}