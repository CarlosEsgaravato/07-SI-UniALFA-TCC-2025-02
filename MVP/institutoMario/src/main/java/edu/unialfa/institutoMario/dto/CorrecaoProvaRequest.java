package edu.unialfa.institutoMario.dto;

import lombok.Data;

import java.util.List;
import java.util.ArrayList;

@Data
public class CorrecaoProvaRequest {
    private Long idProva;
    private Long idAluno;
    private List<RespostaSimplesDTO> respostas = new ArrayList<>();
}
