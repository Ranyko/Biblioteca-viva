package br.com.bibliotecaviva.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ConfiguracaoBibliotecaResponse(int id, int prazoEmprestimoDias, int limiteEmprestimos,
        BigDecimal valorMultaDia, BigDecimal valorMaximoMulta, int prazoRetiradaReservaDias,
        OffsetDateTime atualizadoEm) { }
