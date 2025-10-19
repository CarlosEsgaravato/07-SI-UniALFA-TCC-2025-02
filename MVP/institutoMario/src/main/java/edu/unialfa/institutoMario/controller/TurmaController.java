package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/turmas")
public class TurmaController {

    private final TurmaService turmaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("turmas", turmaService.listarTodas());
        return "turmas/lista";
    }

    @GetMapping("/nova")
    public String novaTurmaForm(Model model) {
        model.addAttribute("turma", new Turma());
        return "turmas/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Turma turma) {
        turmaService.salvar(turma);
        return "redirect:/turmas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("turma", turmaService.buscarPorId(id));
        return "turmas/formulario";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            turmaService.deletarPorId(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Turma excluida com sucesso!");
        }catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/turmas";
    }

}