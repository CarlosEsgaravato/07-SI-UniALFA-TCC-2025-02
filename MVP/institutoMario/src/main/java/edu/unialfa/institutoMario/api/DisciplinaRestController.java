package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Usuario; // Importe seu modelo Usuario
import edu.unialfa.institutoMario.service.DisciplinaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importante
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@AllArgsConstructor
@RequestMapping("/api/disciplinas")
@CrossOrigin(origins = "*")
public class DisciplinaRestController {
    private final DisciplinaService disciplinaService;

    @GetMapping("/professor/{id}")
    public ResponseEntity<List<Disciplina>> listarDisciplinasPorProfessor(@PathVariable Long id) {
        List<Disciplina> disciplinas = disciplinaService.listarPorProfessor(id);
        return ResponseEntity.ok(disciplinas);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Disciplina>> listarMinhasDisciplinas(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        List<Disciplina> disciplinas = disciplinaService.listarPorProfessor(usuario.getId());

        return ResponseEntity.ok(disciplinas);
    }
}

