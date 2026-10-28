package br.com.bibliotecaviva.dto;

import java.util.List;

public record PainelBibliotecaResponse(
        Long emprestimosAtivos,
        Long emprestimosAtrasados,
        List<ObraMaisEmprestadaResponse> rankingObras,
        List<EmprestimosPorCategoriaResponse> emprestimosPorCategoria) {
}