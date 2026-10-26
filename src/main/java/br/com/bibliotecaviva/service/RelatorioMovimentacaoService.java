package br.com.bibliotecaviva.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.MovimentacaoCsvResponse;
import br.com.bibliotecaviva.repository.RelatorioMovimentacaoRepository;

@Service
public class RelatorioMovimentacaoService {

    private final RelatorioMovimentacaoRepository repository;

    public RelatorioMovimentacaoService(
            RelatorioMovimentacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public byte[] exportarCsv(LocalDate dataInicio, LocalDate dataFim) {
        validarPeriodo(dataInicio, dataFim);

        List<MovimentacaoCsvResponse> movimentacoes =
                repository.buscarMovimentacoes(dataInicio, dataFim);

        StringBuilder csv = new StringBuilder();

        csv.append("Período;Obra;Leitor;Retirada;Prazo;Devolução;Situação\n");

        String periodo = formatarPeriodo(dataInicio, dataFim);

        for (MovimentacaoCsvResponse movimentacao : movimentacoes) {
            adicionarLinha(csv,
                    periodo,
                    movimentacao.tituloObra(),
                    movimentacao.nomeLeitor(),
                    formatarDataHora(movimentacao.dataRetirada()),
                    formatarData(movimentacao.dataPrevistaDevolucao()),
                    formatarDataHora(movimentacao.dataDevolucao()),
                    movimentacao.dataDevolucao() == null
                            ? "EM ANDAMENTO"
                            : "DEVOLVIDO");
        }

        byte[] conteudo = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

        byte[] arquivo = new byte[bom.length + conteudo.length];

        System.arraycopy(bom, 0, arquivo, 0, bom.length);
        System.arraycopy(conteudo, 0, arquivo, bom.length, conteudo.length);

        return arquivo;
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final.");
        }
    }

    private String formatarPeriodo(LocalDate inicio, LocalDate fim) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (inicio == null && fim == null) {
            return "Todos os períodos";
        }

        if (inicio == null) {
            return "Até " + fim.format(formato);
        }

        if (fim == null) {
            return "A partir de " + inicio.format(formato);
        }

        return inicio.format(formato) + " a " + fim.format(formato);
    }

    private String formatarData(LocalDate data) {
        return data == null
                ? ""
                : data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatarDataHora(java.time.OffsetDateTime data) {
        return data == null
                ? ""
                : data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void adicionarLinha(StringBuilder csv, String... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                csv.append(';');
            }

            csv.append(escaparCsv(valores[i]));
        }

        csv.append('\n');
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }

        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}