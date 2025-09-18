package edu.unialfa.institutoMario.dto;

import edu.unialfa.institutoMario.model.Evento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoDto {
    private Long id;
    private String nomeEvento;
    private Long idTurma;
    private LocalDate data;
    private String local;
    private String observacao;
    private String imagemUrl;


    public static EventoDto fromEntity(Evento evento){
        Long turmaId = (evento.getTurma() != null) ? evento.getTurma().getId() : null;
        return new EventoDto(
                evento.getId(),
                evento.getNomeEvento(),
                turmaId,
                evento.getData(),
                evento.getLocal(),
                evento.getObservacao(),
                evento.getImagemUrl()
        );
    }
}
