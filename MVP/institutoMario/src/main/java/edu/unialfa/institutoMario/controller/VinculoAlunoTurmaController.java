package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.AlunoService;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vinculo-aluno-turma")
@AllArgsConstructor
public class VinculoAlunoTurmaController {

    private final AlunoService alunoService;
    private final TurmaService turmaService;

    @GetMapping("/turma/{id}")
    public String formularioDeVinculo(@PathVariable Long id, Model model) {
        Turma turma = turmaService.buscarPorId(id);
        model.addAttribute("turma", turma);
        model.addAttribute("alunosDisponiveis", alunoService.listarAlunosSemTurma());
        model.addAttribute("alunosVinculados", alunoService.listarPorTurma(turma));
        return "vinculoAlunosTurmas/formulario";
    }


    @PostMapping("/salvar")
    public String vincularAluno(@RequestParam Long alunoId, @RequestParam Long turmaId) {
        Aluno aluno = alunoService.buscarPorId(alunoId);
        Turma turma = turmaService.buscarPorId(turmaId);

        aluno.setTurma(turma);
        alunoService.salvar(aluno);

        return "redirect:/vinculo-aluno-turma/turma/" + turmaId;
    }

    @GetMapping("/desvincular")
    public String desvincular(@RequestParam Long turmaId, @RequestParam Long alunoId) {
        Aluno aluno = alunoService.buscarPorId(alunoId);

        aluno.setTurma(null);
        alunoService.salvar(aluno);

        return "redirect:/vinculo-aluno-turma/turma/" + turmaId;
    }

}

