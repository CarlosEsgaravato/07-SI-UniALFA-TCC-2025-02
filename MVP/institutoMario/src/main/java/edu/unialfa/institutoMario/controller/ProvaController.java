package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.service.DisciplinaService;
import edu.unialfa.institutoMario.service.ProfessorService;
import edu.unialfa.institutoMario.service.ProvaService;
import edu.unialfa.institutoMario.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/provas")
public class ProvaController {
    private final ProvaService provaService;
    private final DisciplinaService disciplinaService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        Usuario usuarioLogado = usuarioService.getUsuarioLogado();
        List<Prova> provas;

        if (usuarioLogado.getTipoUsuario().getDescricao().equalsIgnoreCase("ADMIN")) {
            provas = provaService.listarTodos();
        } else {
            provas = provaService.listarPorProfessor(usuarioLogado.getId());
        }

        model.addAttribute("provas", provas);

        return "provas/lista";
    }

    @GetMapping("/nova")
    public String novaProvaForm(Model model) {
        Usuario usuarioLogado = usuarioService.getUsuarioLogado();
        List<Disciplina> disciplinas;

        if (usuarioLogado.getTipoUsuario().getDescricao().equalsIgnoreCase("ADMIN")) {
            disciplinas = disciplinaService.listarTodas();
        } else {
            disciplinas = disciplinaService.listarPorProfessor(usuarioLogado.getId());
        }

        model.addAttribute("prova", new Prova());
        model.addAttribute("disciplinas", disciplinas);

        return "provas/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Prova prova) {
        provaService.salvar(prova);

        return "redirect:/provas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuarioLogado = usuarioService.getUsuarioLogado();
        List<Disciplina> disciplinas;

        if (usuarioLogado.getTipoUsuario().getDescricao().equalsIgnoreCase("ADMIN")) {
            disciplinas = disciplinaService.listarTodas();
        } else {
            disciplinas = disciplinaService.listarPorProfessor(usuarioLogado.getId());
        }

        model.addAttribute("prova", provaService.buscarPorId(id));
        model.addAttribute("disciplinas", disciplinas);

        return "provas/formulario";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        provaService.deletarPorId(id);

        return "redirect:/provas";
    }
}
