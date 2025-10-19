package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.service.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import edu.unialfa.institutoMario.service.PdfService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/provas")
public class ProvaController {
    private final ProvaService provaService;
    private final DisciplinaService disciplinaService;
    private final UsuarioService usuarioService;
    private final AlunoService alunoService;
    private final RespostaAlunoService respostaAlunoService;
    private final PdfService pdfService;

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

    @GetMapping("/responder")
    public String listarProvasParaResponder(Model model, @AuthenticationPrincipal Usuario usuarioLogado) {
        Aluno aluno = alunoService.buscarPorUsuario(usuarioLogado);
        Long alunoId = aluno.getId();
        List<Prova> provas;
        if(aluno.getTurma() != null){
            Long turmaId = aluno.getTurma().getId();
            provas = provaService.listarPorTurma(turmaId);
        }else {
            provas = new ArrayList<>();
        }

        List<Long> provasRespondidasIds = respostaAlunoService.buscarIdsDeProvasRespondidas(alunoId);

        model.addAttribute("provas", provas);
        model.addAttribute("alunoId", alunoId);
        model.addAttribute("provasRespondidasIds", provasRespondidasIds);

        return "aluno/lista-provas";
    }

    @GetMapping("/exportar/{id}")
    public ResponseEntity<InputStreamResource> exportarProvaPdf(@PathVariable Long id) {
        Prova prova = provaService.buscarPorId(id);
        ByteArrayInputStream pdf = pdfService.gerarPdfDaProva(prova);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=prova-" + prova.getId() + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}