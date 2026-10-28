package br.com.bibliotecaviva.dto;

import java.util.List;

public record PainelLeitorResponse(
        List<EmprestimoLeitorResponse> emprestimos,
        List<ReservaLeitorResponse> reservas,
        List<MultaLeitorResponse> multas) {
}
