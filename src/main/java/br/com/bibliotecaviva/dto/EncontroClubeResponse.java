package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record EncontroClubeResponse(
        long id,
        long clubeId,
        String clubeNome,
        long livroId,
        String livroTitulo,
        OffsetDateTime dataHora,
        String localOuAcesso,
        String status,
        int capacidadeMaxima,
        long inscricoesConfirmadas,
        int vagasDisponiveis,
        OffsetDateTime criadoEm) { }
