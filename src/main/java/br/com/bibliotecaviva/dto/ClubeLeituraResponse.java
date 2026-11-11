package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ClubeLeituraResponse(
        long id,
        String nome,
        String descricao,
        String status,
        int capacidadeMaxima,
        String periodicidade,
        long responsavelId,
        OffsetDateTime criadoEm,
        List<ReferenciaResponse> categorias) { }
