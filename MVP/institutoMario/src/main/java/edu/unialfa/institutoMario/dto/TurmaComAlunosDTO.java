package edu.unialfa.institutoMario.dto;

import edu.unialfa.institutoMario.model.Aluno;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class TurmaComAlunosDTO {

    private Long id;
    private String nome;
    private List<AlunoSimplesDTO> alunos;

    public static TurmaComAlunosDTO fromEntity(Long id, String nome, List<Aluno> alunos) {
        List<AlunoSimplesDTO> dtoList = alunos.stream()
                .map(AlunoSimplesDTO::fromEntity)
                .collect(Collectors.toList());

        return new TurmaComAlunosDTO(id, nome, dtoList);
    }

    @Data
    @AllArgsConstructor
    public static class AlunoSimplesDTO {
        private Long id;
        private String tipoUsuario;
        private String nome;
        private String telefone;
        private String email;

        public static AlunoSimplesDTO fromEntity(Aluno aluno) {
            var usuario = aluno.getUsuario();
            return new AlunoSimplesDTO(
                    aluno.getId(),
                    usuario.getTipoUsuario().getDescricao(),
                    usuario.getNome(),
                    usuario.getTelefone(),
                    usuario.getEmail()
            );
        }
    }
}
