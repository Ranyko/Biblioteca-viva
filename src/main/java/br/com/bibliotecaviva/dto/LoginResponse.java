package br.com.bibliotecaviva.dto;

import br.com.bibliotecaviva.model.PerfilUsuario;

public record LoginResponse(
        String token,
        String tipo,
        Long usuarioId,
        String nome,
        String email,
        PerfilUsuario perfil) {
}
