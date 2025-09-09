package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.service.DisciplinaService;
import edu.unialfa.institutoMario.service.ProfessorService;
import edu.unialfa.institutoMario.service.TurmaService;
import edu.unialfa.institutoMario.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;
    private final TurmaService turmaService;
    private final ProfessorService professorService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        Usuario usuarioLogado = usuarioService.getUsuarioLogado();
        List<Disciplina> disciplinas;

        if (usuarioLogado.getTipoUsuario().getDescricao().equalsIgnoreCase("ADMIN")) {
            disciplinas = disciplinaService.listarTodas();
        } else if (usuarioLogado.getTipoUsuario().getDescricao().equalsIgnoreCase("PROFESSOR")) {
            disciplinas = disciplinaService.listarPorProfessor(usuarioLogado.getId());
        } else {
            disciplinas = disciplinaService.listarPorAluno(usuarioLogado.getId());
        }

        model.addAttribute("disciplinas", disciplinas);
        return "disciplinas/lista";
    }


    @GetMapping("/nova")
    public String novaDisciplinaForm(Model model) {
        model.addAttribute("disciplina", new Disciplina());
        model.addAttribute("turmas", turmaService.listarTodas());
        model.addAttribute("professores", professorService.listarTodos());
        return "disciplinas/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Disciplina disciplina) {
        disciplinaService.salvar(disciplina);
        return "redirect:/disciplinas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("disciplina", disciplinaService.buscarPorId(id));
        model.addAttribute("turmas", turmaService.listarTodas());
        model.addAttribute("professores", professorService.listarTodos());
        return "disciplinas/formulario";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        disciplinaService.deletarPorId(id);
        return "redirect:/disciplinas";
    }
}