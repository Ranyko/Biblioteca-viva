package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

import br.com.bibliotecaviva.model.Leitor;

public record LeitorResponse(
        Long id,
        Long usuarioId,
        String nome,
        String email,
        String documento,
        String telefone,
        boolean ativo,
        OffsetDateTime cadastradoEm) {

    public static LeitorResponse from(Leitor leitor) {
        return new LeitorResponse(
                leitor.getId(),
                leitor.getUsuario().getId(),
                leitor.getUsuario().getNome(),
                leitor.getUsuario().getEmail(),
                leitor.getDocumento(),
                leitor.getTelefone(),
                leitor.getUsuario().isAtivo(),
                leitor.getCadastradoEm());
    }
}
