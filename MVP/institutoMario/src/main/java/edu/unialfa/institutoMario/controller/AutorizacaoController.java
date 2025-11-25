package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.dto.DadosAutorizacao;
import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Turma;
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
    public String gerarImpressao(DadosAutorizacao dados, Model model, RedirectAttributes redirectAttributes) {
        if (dados.getTurmaId() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Por favor, selecione uma turma.");
            return "redirect:/autorizacoes/nova";
        }

        Turma turma = turmaService.buscarPorId(dados.getTurmaId());
        List<Aluno> alunos = alunoService.listarAlunosPorTurmaId(dados.getTurmaId());

        if (alunos.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "A turma selecionada (" + turma.getNome() + ") não possui alunos vinculados.");
            return "redirect:/autorizacoes/nova";
        }

        model.addAttribute("alunos", alunos);
        model.addAttribute("turmaNome", turma.getNome()); // Passamos o nome da turma direto
        model.addAttribute("dados", dados);

        return "relatorios/impressao-autorizacao";
    }

    @PostMapping("/enviar-email")
    public String enviarEmails(DadosAutorizacao dados, RedirectAttributes redirectAttributes) {
        if (dados.getTurmaId() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Selecione uma turma para enviar os e-mails.");
            return "redirect:/autorizacoes/nova";
        }

        List<Aluno> alunos = alunoService.listarAlunosPorTurmaId(dados.getTurmaId());
        if (alunos.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Não há alunos nesta turma para enviar e-mails.");
            return "redirect:/autorizacoes/nova";
        }

        try {
            autorizacaoService.enviarEmailsAutorizacao(dados);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Processo de envio finalizado. Verifique o console para detalhes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro crítico ao enviar e-mails: " + e.getMessage());
        }
        return "redirect:/autorizacoes/nova";
    }
}