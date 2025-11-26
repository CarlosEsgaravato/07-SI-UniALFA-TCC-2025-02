package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.AlunoService;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vinculo-aluno-turma")
@AllArgsConstructor
public class VinculoAlunoTurmaController {

    private final AlunoService alunoService;
    private final TurmaService turmaService;

    @GetMapping("/turma/{id}")
    public String formularioDeVinculo(@PathVariable Long id, Model model) {
        Turma turma = turmaService.buscarPorId(id);

        List<Aluno> todosAlunos = alunoService.listarTodos();
        List<Aluno> alunosDisponiveis = todosAlunos.stream()
                .filter(a -> !a.getTurmas().contains(turma))
                .collect(Collectors.toList());
        List<Aluno> alunosVinculados = turma.getAlunos();
        model.addAttribute("turma", turma);
        model.addAttribute("alunosDisponiveis", alunosDisponiveis);
        model.addAttribute("alunosVinculados", alunosVinculados);

        return "vinculoAlunosTurmas/formulario";
    }


    @PostMapping("/salvar")
    public String vincularAluno(@RequestParam Long alunoId, @RequestParam Long turmaId) {
        Aluno aluno = alunoService.buscarPorId(alunoId);
        Turma turma = turmaService.buscarPorId(turmaId);
        if (!aluno.getTurmas().contains(turma)) {
            aluno.getTurmas().add(turma);
            alunoService.salvar(aluno);
        }

        return "redirect:/vinculo-aluno-turma/turma/" + turmaId;
    }

    @GetMapping("/desvincular")
    public String desvincular(@RequestParam Long turmaId, @RequestParam Long alunoId) {
        Aluno aluno = alunoService.buscarPorId(alunoId);
        Turma turma = turmaService.buscarPorId(turmaId);
        aluno.getTurmas().remove(turma);
        alunoService.salvar(aluno);

        return "redirect:/vinculo-aluno-turma/turma/" + turmaId;
    }
}