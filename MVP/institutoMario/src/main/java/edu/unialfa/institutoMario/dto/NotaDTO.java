package edu.unialfa.institutoMario.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class NotaDTO {
    private String alunoNome;
    private String disciplinaNome;
    private LocalDate data;
    private BigDecimal notaTotal;
}
