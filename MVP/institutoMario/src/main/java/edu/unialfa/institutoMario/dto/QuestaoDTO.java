package edu.unialfa.institutoMario.dto;

import edu.unialfa.institutoMario.model.Questao;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class QuestaoDTO {
    private Long id;
    private String numero;
    private BigDecimal pontuacao;
    private String alternativaCorreta;

    public static QuestaoDTO fromEntity(Questao questao) {
        return new QuestaoDTO(
                questao.getId(),
                questao.getNumero(),
                questao.getPontuacao(),
                questao.getAlternativaCorreta()
        );
    }
}
