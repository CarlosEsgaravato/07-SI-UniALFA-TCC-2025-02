package edu.unialfa.institutoMario.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DadosAutorizacao {
    private Long turmaId;
    private String tituloEvento;
    private String local;
    private String descricao;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataEvento;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horarioSaida;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horarioRetorno;
}
