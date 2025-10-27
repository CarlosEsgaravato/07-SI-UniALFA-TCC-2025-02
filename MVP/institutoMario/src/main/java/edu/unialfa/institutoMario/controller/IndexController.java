package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Evento;
import edu.unialfa.institutoMario.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class IndexController {

    private final TurmaService turmaService;
    private final AlunoService alunoService;
    private final NotaService notaService;
    private final EventoService eventoService;
    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String index(Model model) {


        long turmasCount = turmaService.contarTodasTurmas();

        long alunosCount = alunoService.contarTodosAlunos();

        String mediaNotasGeral = notaService.calcularMediaGeral();

        Map<String, String> mediaNotasPorTurma = notaService.calcularMediaPorTurma();

        List<Evento> proximosEventos = eventoService.buscarProximosEventos(5);

        model.addAttribute("turmasCount", turmasCount);
        model.addAttribute("alunosCount", alunosCount);
        model.addAttribute("mediaNotasGeral", mediaNotasGeral);
        model.addAttribute("mediaNotasPorTurma", mediaNotasPorTurma);
        model.addAttribute("proximosEventos", proximosEventos);
        model.addAttribute("usuarioLogado",usuarioService.getUsuarioLogado());

        return "index";
    }
}