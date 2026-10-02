package br.com.bibliotecaviva.dto;

import java.math.BigDecimal;

public record DevolucaoResponse(EmprestimoResponse emprestimo, long diasAtraso,
        BigDecimal valorMulta, String statusExemplar, Long reservaDisponibilizadaId) { }
