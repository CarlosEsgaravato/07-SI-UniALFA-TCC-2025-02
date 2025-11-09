package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.dto.RespostaSimplesDTO;
import edu.unialfa.institutoMario.model.RespostaAluno;
import edu.unialfa.institutoMario.service.RespostaAlunoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/respostas")
@CrossOrigin(origins = "*")
public class RespostaAlunoController {

    private final RespostaAlunoService respostaAlunoService;

    @GetMapping("/aluno/{alunoId}/prova/{provaId}")
    public ResponseEntity<List<RespostaSimplesDTO>> buscarRespostasDoAluno(
            @PathVariable Long alunoId,
            @PathVariable Long provaId) {

        List<RespostaAluno> respostas = respostaAlunoService.buscarPorAlunoEProva(alunoId, provaId);

        List<RespostaSimplesDTO> dtos = respostas.stream()
                .map(resposta -> new RespostaSimplesDTO(
                        resposta.getNumeroQuestao(),
                        resposta.getAlternativaEscolhida()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/prova/{provaId}/alunos")
    public ResponseEntity<List<Long>> buscarIdsAlunosPorProva(@PathVariable Long provaId) {
        List<Long> ids = respostaAlunoService.buscarIdsAlunosPorProva(provaId);
        return ResponseEntity.ok(ids);
    }
}