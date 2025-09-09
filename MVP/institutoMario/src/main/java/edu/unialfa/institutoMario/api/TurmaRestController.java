package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.dto.DisciplinaComProfessorDto;
import edu.unialfa.institutoMario.dto.TurmaComAlunosDTO;
import edu.unialfa.institutoMario.dto.TurmaDTO;
import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.AlunoService;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/turmas")
public class TurmaRestController {
    private final TurmaService turmaService;
    private final AlunoService alunoService;

    @GetMapping
    public ResponseEntity<List<TurmaDTO>> listarTodos() {
        List<Turma> turmas = turmaService.listarTodas();

        List<TurmaDTO> dtos = turmas.stream()
                .map(t -> new TurmaDTO(t.getId(), t.getNome()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/disciplinas")
    public ResponseEntity<List<DisciplinaComProfessorDto>> listarDisciplinas(@PathVariable Long id) {
        List<Disciplina> disciplinas = turmaService.listarDisciplinasPorTurma(id);

        List<DisciplinaComProfessorDto> dto = disciplinas.stream()
                .map(DisciplinaComProfessorDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/alunos")
    public ResponseEntity<TurmaComAlunosDTO> listarAlunos(@PathVariable Long id) {
        Turma turma = turmaService.buscarPorId(id);
        List<Aluno> alunos = alunoService.listarPorTurma(turma);
        TurmaComAlunosDTO dto = TurmaComAlunosDTO.fromEntity(turma.getId(), turma.getNome(), alunos);

        return ResponseEntity.ok(dto);
    }

}
