package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.dto.ProvaComQuestoesDTO; // Importe o DTO
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.service.ProvaService;
import edu.unialfa.institutoMario.service.QuestaoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
@RequestMapping("/questoes")
public class QuestaoController {

    private final ProvaService provaService;
    private final QuestaoService questaoService;

    @GetMapping("/prova/{id}/gerenciar")
    public String gerenciarQuestoes(@PathVariable Long id, Model model) {
        Prova prova = provaService.buscarPorId(id);

        ProvaComQuestoesDTO dto = new ProvaComQuestoesDTO();
        dto.setProvaId(prova.getId());
        dto.setQuestoes(questaoService.listarPorProvaId(id));

        model.addAttribute("prova", prova);
        model.addAttribute("provaComQuestoes", dto);

        return "questoes/formulario";
    }

    @PostMapping("/salvar-tudo")
    public String salvarTudo(@ModelAttribute ProvaComQuestoesDTO provaComQuestoes) {
        questaoService.atualizarQuestoesDaProva(provaComQuestoes);
        return "redirect:/provas";
    }
}