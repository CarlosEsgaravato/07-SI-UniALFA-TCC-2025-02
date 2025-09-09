package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.dto.CorrecaoProvaRequest;
import edu.unialfa.institutoMario.service.RespostaAlunoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/correcao")
public class CorrecaoProvaController {
    private final RespostaAlunoService respostaAlunoService;

    @PostMapping("/corrigir")
    public ResponseEntity<String> corrigirProva(@RequestBody CorrecaoProvaRequest correcaoRequest) {
        respostaAlunoService.processarRespostas(correcaoRequest);

        return ResponseEntity.ok("Respostas processadas com sucesso");
    }
}
