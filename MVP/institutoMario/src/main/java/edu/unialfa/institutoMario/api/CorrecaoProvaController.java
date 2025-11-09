package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.dto.CorrecaoProvaRequest;
import edu.unialfa.institutoMario.service.RespostaAlunoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/correcao")
@CrossOrigin(origins = "*")
public class CorrecaoProvaController {
    private final RespostaAlunoService respostaAlunoService;

    @PostMapping("/corrigir")
    public ResponseEntity<Map<String, String>> corrigirProva(@RequestBody CorrecaoProvaRequest correcaoRequest) {
        respostaAlunoService.processarRespostas(correcaoRequest);
        Map<String, String> responseBody = Map.of(
                "status", "success",
                "message", "Respostas processadas com sucesso"
        );
        return ResponseEntity.ok(responseBody);
    }
}