package br.com.bibliotecaviva.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.bibliotecaviva.dto.ClubeLeituraResponse;
import br.com.bibliotecaviva.dto.EncontroClubeResponse;
import br.com.bibliotecaviva.dto.InscricaoEncontroResponse;
import br.com.bibliotecaviva.dto.MinhaInscricaoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.ClubeLeituraService;

@RestController
@RequestMapping("/leitor")
public class LeitorClubeLeituraController {
    private final ClubeLeituraService service;

    public LeitorClubeLeituraController(ClubeLeituraService service) {
        this.service = service;
    }

    @GetMapping("/clubes-leitura")
    public PaginaResponse<ClubeLeituraResponse> clubes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listarClubes(false, nome, categoriaId, pagina, tamanho);
    }

    @GetMapping("/clubes-leitura/{clubeId}/encontros")
    public PaginaResponse<EncontroClubeResponse> encontros(@PathVariable long clubeId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listarEncontrosDoClube(clubeId, pagina, tamanho);
    }

    @GetMapping("/clubes-leitura/{clubeId}/encontros/{encontroId}")
    public EncontroClubeResponse encontro(@PathVariable long clubeId, @PathVariable long encontroId) {
        return service.buscarEncontroDoLeitor(clubeId, encontroId);
    }

    @PostMapping("/encontros-clube/{encontroId}/inscricao")
    public ResponseEntity<InscricaoEncontroResponse> inscrever(@PathVariable long encontroId,
            @AuthenticationPrincipal Usuario usuario) {
        InscricaoEncontroResponse inscricao = service.inscrever(encontroId, usuario.getId());
        return ResponseEntity.created(URI.create("/leitor/encontros-clube/" + encontroId
            + "/inscricao")).body(inscricao);
    }

    @DeleteMapping("/encontros-clube/{encontroId}/inscricao")
    public InscricaoEncontroResponse cancelar(@PathVariable long encontroId,
            @AuthenticationPrincipal Usuario usuario) {
        return service.cancelarInscricao(encontroId, usuario.getId());
    }

    @GetMapping("/minhas-inscricoes")
    public PaginaResponse<MinhaInscricaoResponse> minhasInscricoes(
            @RequestParam(defaultValue = "false") boolean historico,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @AuthenticationPrincipal Usuario usuario) {
        return service.minhasInscricoes(usuario.getId(), historico, pagina, tamanho);
    }
}
