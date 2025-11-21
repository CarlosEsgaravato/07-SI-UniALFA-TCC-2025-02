package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.dto.DadosAutorizacao;
import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.service.AlunoService;
import edu.unialfa.institutoMario.service.AutorizacaoService;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/autorizacoes")
@AllArgsConstructor
public class AutorizacaoController {

    private final TurmaService turmaService;
    private final AlunoService alunoService;
    private final AutorizacaoService autorizacaoService;

    @GetMapping("/nova")
    public String formularioAutorizacao(Model model) {
        model.addAttribute("turmas", turmaService.listarTodas());
        model.addAttribute("dados", new DadosAutorizacao());
        return "relatorios/form-autorizacao";
    }

    @PostMapping("/imprimir")
    public String gerarImpressao(DadosAutorizacao dados, Model model) {
        List<Aluno> alunos = alunoService.listarAlunosPorTurmaId(dados.getTurmaId());

        model.addAttribute("alunos", alunos);
        model.addAttribute("dados", dados);
        return "relatorios/impressao-autorizacao";
    }

    // 3. Enviar E-mails
    @PostMapping("/enviar-email")
    public String enviarEmails(DadosAutorizacao dados, RedirectAttributes redirectAttributes) {
        try {
            autorizacaoService.enviarEmailsAutorizacao(dados);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "E-mails enviados para os responsáveis com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao enviar e-mails: " + e.getMessage());
        }
        return "redirect:/autorizacoes/nova";
    }
}