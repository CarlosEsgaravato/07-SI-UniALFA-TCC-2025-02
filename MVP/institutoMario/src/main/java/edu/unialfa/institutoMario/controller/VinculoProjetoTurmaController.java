package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Projetos;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.ProjetoService;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vinculo-projeto-turma")
@RequiredArgsConstructor
public class VinculoProjetoTurmaController {

    private final ProjetoService projetoService;
    private final TurmaService turmaService;

    @GetMapping("/projeto/{id}")
    public String formularioDeVinculo(@PathVariable Long id, Model model) {
        Projetos projeto = projetoService.bucarPorId(id);
        List<Turma> todasTurmas = turmaService.listarTodas();
        List<Turma> turmasDisponiveis = todasTurmas.stream()
                .filter(t -> !projeto.getTurmas().contains(t))
                .collect(Collectors.toList());
        model.addAttribute("projeto", projeto);
        model.addAttribute("turmasDisponiveis", turmasDisponiveis);
        model.addAttribute("turmasVinculadas", projeto.getTurmas());
        return "vinculoProjetoTurma/formulario";
    }

    @PostMapping("/salvar")
    public String vincularTurma(@RequestParam Long projetoId, @RequestParam Long turmaId) {
        Projetos projeto = projetoService.bucarPorId(projetoId);
        Turma turma = turmaService.buscarPorId(turmaId);
        projeto.getTurmas().add(turma);
        projetoService.salvar(projeto);
        return "redirect:/vinculo-projeto-turma/projeto/" + projetoId;
    }

    @GetMapping("/desvincular")
    public String desvincular(@RequestParam Long projetoId, @RequestParam Long turmaId) {
        Projetos projeto = projetoService.bucarPorId(projetoId);
        Turma turma = turmaService.buscarPorId(turmaId);
        projeto.getTurmas().remove(turma);
        projetoService.salvar(projeto);
        return "redirect:/vinculo-projeto-turma/projeto/" + projetoId;
    }
}