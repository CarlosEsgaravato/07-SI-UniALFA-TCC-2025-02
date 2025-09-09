package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.model.Questao;
import edu.unialfa.institutoMario.service.ProvaService;
import edu.unialfa.institutoMario.service.QuestaoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/questoes")
public class QuestaoController {

    private final ProvaService provaService;
    private final QuestaoService questaoService;

    @GetMapping("/prova/{id}/cadastrar")
    public String cadastrarQuestoes(@PathVariable Long id, Model model) {
        var prova = provaService.buscarPorId(id);
        var questao = new Questao();
        questao.setProva(prova);

        model.addAttribute("prova", prova);
        model.addAttribute("questao", questao);
        model.addAttribute("questoes", questaoService.listarPorProvaId(id));

        return "questoes/formulario";
    }

    @PostMapping("/salvar")
    public String salvarQuestao(@ModelAttribute Questao questao) {
        questaoService.salvar(questao);
        return "redirect:/questoes/prova/" + questao.getProva().getId() + "/cadastrar";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, @RequestParam("provaId") Long provaId) {
        questaoService.deletarPorId(id);
        return "redirect:/questoes/prova/" + provaId + "/cadastrar";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @RequestParam("provaId") Long provaId, Model model) {
        Questao questao = questaoService.buscarPorId(id);
        Prova prova = provaService.buscarPorId(provaId);
        List<Questao> questoes = questaoService.listarPorProvaId(provaId);

        model.addAttribute("questao", questao);
        model.addAttribute("prova", prova);
        model.addAttribute("questoes", questoes);

        return "questoes/formulario";
    }


}
