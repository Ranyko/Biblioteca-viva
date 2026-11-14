package br.com.bibliotecaviva.dto;

import java.math.BigDecimal;
import java.util.List;

public record AvaliacoesLivroResponse(
        long livroId,
        BigDecimal media,
        long total,
        int pagina,
        int tamanho,
        List<AvaliacaoResponse> itens) {
}
