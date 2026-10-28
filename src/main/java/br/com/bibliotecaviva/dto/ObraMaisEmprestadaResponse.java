package br.com.bibliotecaviva.dto;

public record ObraMaisEmprestadaResponse(
        Long livroId,
        String titulo,
        Long totalEmprestimos) {
}