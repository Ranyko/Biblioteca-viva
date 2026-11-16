package br.com.bibliotecaviva.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConfiguracaoBibliotecaRequest(
        @NotNull(message = "O prazo de empréstimo é obrigatório")
        @Min(value = 1, message = "O prazo deve ser positivo")
        @Max(value = 32767, message = "O prazo deve ser de até 32767 dias")
        Integer prazoEmprestimoDias,

        @NotNull(message = "O limite de empréstimos é obrigatório")
        @Min(value = 1, message = "O limite deve ser positivo")
        @Max(value = 32767, message = "O limite deve ser de até 32767 empréstimos")
        Integer limiteEmprestimos,

        @NotNull(message = "O valor diário é obrigatório")
        @DecimalMin(value = "0.00", message = "O valor diário não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Informe até 8 dígitos inteiros e 2 casas decimais")
        BigDecimal valorMultaDia,

        @NotNull(message = "O teto da multa é obrigatório")
        @DecimalMin(value = "0.00", message = "O teto da multa não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Informe até 8 dígitos inteiros e 2 casas decimais")
        BigDecimal valorMaximoMulta,

        @NotNull(message = "O prazo de retirada é obrigatório")
        @Min(value = 1, message = "O prazo deve ser positivo")
        @Max(value = 32767, message = "O prazo deve ser de até 32767 dias")
        Integer prazoRetiradaReservaDias) { }
