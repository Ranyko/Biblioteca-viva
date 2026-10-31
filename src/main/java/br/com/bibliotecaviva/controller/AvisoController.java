package br.com.bibliotecaviva.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.bibliotecaviva.dto.AvisoPreferencias;
import br.com.bibliotecaviva.dto.AvisoResponse;
import br.com.bibliotecaviva.dto.AtualizacaoResponse;
import br.com.bibliotecaviva.dto.ContagemResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.dto.PendenciasResponse;
import br.com.bibliotecaviva.dto.PreferenciaRequest;
import br.com.bibliotecaviva.dto.VarreduraResponse;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.AvisoService;
import br.com.bibliotecaviva.service.AvisoVarreduraService;
import jakarta.validation.Valid;

@RestController
public class AvisoController {

    private final AvisoService avisoService;
    private final AvisoVarreduraService varreduraService;

    public AvisoController(AvisoService avisoService, AvisoVarreduraService varreduraService) {
        this.avisoService = avisoService;
        this.varreduraService = varreduraService;
    }

    @GetMapping("/leitor/avisos")
    public PaginaResponse<AvisoResponse> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) Boolean lido,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return avisoService.listar(usuario.getId(), lido, pagina, tamanho);
    }

    @PatchMapping("/leitor/avisos/{id}/leitura")
    public AvisoResponse marcarLido(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return avisoService.marcarLido(id, usuario.getId());
    }

    @PostMapping("/leitor/avisos/leitura-todos")
    public AtualizacaoResponse marcarTodosLidos(@AuthenticationPrincipal Usuario usuario) {
        return new AtualizacaoResponse(avisoService.marcarTodosLidos(usuario.getId()));
    }

    @GetMapping("/avisos/nao-lidos/contagem")
    public ContagemResponse contarNaoLidos(@AuthenticationPrincipal Usuario usuario) {
        return new ContagemResponse(avisoService.contarNaoLidos(usuario.getId()));
    }

    @GetMapping("/atendente/avisos/pendencias")
    public PendenciasResponse pendencias() {
        return avisoService.pendencias();
    }

    @GetMapping("/admin/avisos/preferencias")
    public AvisoPreferencias preferencias() {
        return avisoService.preferencias();
    }

    @PutMapping("/admin/avisos/preferencias")
    public AvisoPreferencias atualizarPreferencias(@Valid @RequestBody PreferenciaRequest request) {
        return avisoService.atualizarPreferencias(new AvisoPreferencias(
                request.devidaAmanha(),
                request.vencido(),
                request.reservaDisponivel(),
                request.reservaExpirando(),
                request.reservaExpirada(),
                request.multaGerada(),
                request.filaPosicao(),
                null));
    }

    @PostMapping("/admin/avisos/varredura")
    public ResponseEntity<VarreduraResponse> executarVarredura() {
        return ResponseEntity.ok(varreduraService.executar());
    }
}