package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.model.Professor;
import edu.unialfa.institutoMario.service.ProfessorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/professores")
@CrossOrigin(origins = "*")
public class ProfessorRestController {

    private final ProfessorService professorService;

    @GetMapping
    public ResponseEntity<List<Professor>> listarTodos(){
        List<Professor> professores = professorService.listarTodos();
        return ResponseEntity.ok(professores);
    }
}
