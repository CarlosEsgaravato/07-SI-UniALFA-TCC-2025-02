package edu.unialfa.institutoMario.dto;

import edu.unialfa.institutoMario.model.Questao;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProvaComQuestoesDTO {
    private Long provaId;
    private List<Questao> questoes = new ArrayList<>();
}