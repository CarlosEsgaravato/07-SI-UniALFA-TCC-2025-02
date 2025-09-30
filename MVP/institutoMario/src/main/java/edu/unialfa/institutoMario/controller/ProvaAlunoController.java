package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.dto.CorrecaoProvaRequest;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.service.ProvaService;
import edu.unialfa.institutoMario.service.RespostaAlunoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import edu.unialfa.institutoMario.model.Usuario;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/responder")
public class ProvaAlunoController {

    private final ProvaService provaService;
    private final RespostaAlunoService respostaAlunoService;

    @GetMapping("/prova/{provaId}")
    public String exibirProvaParaResponder(@PathVariable Long provaId, @RequestParam Long alunoId, Model model) {
        Prova prova = provaService.buscarPorId(provaId);
        CorrecaoProvaRequest correcaoRequest = new CorrecaoProvaRequest();
        correcaoRequest.setIdProva(provaId);
        correcaoRequest.setIdAluno(alunoId);

        model.addAttribute("prova", prova);
        model.addAttribute("correcaoRequest", correcaoRequest);

        return "aluno/responder-prova";
    }
    @PostMapping("/submeter")
    public String submeterRespostas(@ModelAttribute CorrecaoProvaRequest correcaoRequest) {
        respostaAlunoService.processarRespostas(correcaoRequest);
        return "redirect:/responder/sucesso";
    }

    @GetMapping("/sucesso")
    public String paginaSucesso() {
        return "aluno/prova-enviada-sucesso";
    }
    @GetMapping("/provas/responder")
    public String listarProvasParaResponder(Model model, @AuthenticationPrincipal Usuario usuarioLogado) {
        List<Prova> provas = provaService.listarTodos();
        Long alunoId = usuarioLogado.getId();

        model.addAttribute("provas", provas);
        model.addAttribute("alunoId", alunoId);

        return "aluno/lista-provas";
    }
}