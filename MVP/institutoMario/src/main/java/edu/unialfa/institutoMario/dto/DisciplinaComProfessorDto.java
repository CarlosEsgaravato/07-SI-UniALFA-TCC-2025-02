package edu.unialfa.institutoMario.dto;

import edu.unialfa.institutoMario.model.Disciplina;

public record DisciplinaComProfessorDto(
        Long id,
        String nome,
        String nomeProfessor
) {
    public static DisciplinaComProfessorDto fromEntity(Disciplina d) {
        return new DisciplinaComProfessorDto(
                d.getId(),
                d.getNome(),
                d.getProfessor().getUsuario().getNome()
        );
    }
}

