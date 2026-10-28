package br.com.bibliotecaviva.controller;

import java.net.URI;
import java.util.List;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import br.com.bibliotecaviva.dto.PainelBibliotecaResponse;
import br.com.bibliotecaviva.dto.UsuarioCreateRequest;
import br.com.bibliotecaviva.dto.UsuarioResponse;
import br.com.bibliotecaviva.dto.UsuarioUpdateRequest;
import br.com.bibliotecaviva.service.PainelBibliotecaService;
import br.com.bibliotecaviva.service.UsuarioService;
import br.com.bibliotecaviva.service.RelatorioMovimentacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final PainelBibliotecaService painelBibliotecaService;
    private final RelatorioMovimentacaoService relatorioMovimentacaoService;

    public AdminController(
        UsuarioService usuarioService,
        PainelBibliotecaService painelBibliotecaService,
        RelatorioMovimentacaoService relatorioMovimentacaoService) {

        this.usuarioService = usuarioService;
        this.painelBibliotecaService = painelBibliotecaService;
        this.relatorioMovimentacaoService = relatorioMovimentacaoService;
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

    @GetMapping("/relatorios/movimentacoes/csv")
    public ResponseEntity<byte[]> exportarMovimentacoesCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFim) {

        byte[] arquivo = relatorioMovimentacaoService.exportarCsv(
                dataInicio, dataFim);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"movimentacoes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(arquivo);
    }
}