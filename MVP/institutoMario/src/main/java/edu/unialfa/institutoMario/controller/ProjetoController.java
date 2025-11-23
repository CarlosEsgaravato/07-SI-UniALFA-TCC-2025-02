package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Documento;
import edu.unialfa.institutoMario.model.Projetos;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.ProjetoService;
import edu.unialfa.institutoMario.service.TurmaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/projetos")
@PreAuthorize("hasRole('ADMIN')")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final TurmaService turmaService;
    private final String uploadDir;

    public ProjetoController(ProjetoService projetoService,
                             TurmaService turmaService,
                             @Value("${upload.dir}") String uploadDir) {
        this.projetoService = projetoService;
        this.turmaService = turmaService;
        this.uploadDir = uploadDir;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listarProjetos(Model model) {
        model.addAttribute("projetos", projetoService.listarTodos());
        return "projetos/lista";
    }

    @GetMapping("/novo")
    @PreAuthorize("hasRole('ADMIN')")
    public String novoProjetoForm(Model model) {
        model.addAttribute("projeto", new Projetos());
        model.addAttribute("turmas", turmaService.listarTodas());
        return "projetos/formulario";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarProjetoForm(@PathVariable Long id, Model model) {
        Projetos projeto = projetoService.bucarPorId(id);
        model.addAttribute("projeto", projeto);
        model.addAttribute("turmas", turmaService.listarTodas());
        return "projetos/formulario";
    }

    @GetMapping("/documento/deletar/{idDocumento}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletarDocumento(@PathVariable Long idDocumento) {
        Long projetoId = projetoService.deletarDocumento(idDocumento, uploadDir);

        return "redirect:/projetos/editar/" + projetoId;
    }


    @PostMapping("/salvar")
    @PreAuthorize("hasRole('ADMIN')")
    public String salvarProjeto(@Valid @ModelAttribute("projeto") Projetos projeto,
                                BindingResult result,
                                @RequestParam("documentosFiles") List<MultipartFile> documentosFiles,
                                Model model) {

        if (result.hasErrors()) {
            model.addAttribute("turmas", turmaService.listarTodas());
            return "projetos/formulario";
        }

        Projetos projetoParaSalvar;

        if (projeto.getId() != null) {
            projetoParaSalvar = projetoService.bucarPorId(projeto.getId());
            projetoParaSalvar.setNomeProjeto(projeto.getNomeProjeto());
            projetoParaSalvar.setTurmas(projeto.getTurmas());
        } else {
            projetoParaSalvar = projeto;
            if (projetoParaSalvar.getDocumentos() == null) {
                projetoParaSalvar.setDocumentos(new ArrayList<>());
            }
        }

        // --- (O código de upload de arquivos permanece igual) ---
        for (MultipartFile file : documentosFiles) {
            if (file != null && !file.isEmpty()) {
                try {
                    String nomeArquivo = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path caminhoArquivo = Paths.get(uploadDir, nomeArquivo);
                    Files.createDirectories(caminhoArquivo.getParent());
                    Files.copy(file.getInputStream(), caminhoArquivo);
                    Documento doc = new Documento();
                    doc.setNomeArquivo(file.getOriginalFilename());
                    doc.setTipoArquivo(file.getContentType());
                    doc.setUrlArquivo(nomeArquivo);
                    doc.setProjeto(projetoParaSalvar);
                    projetoParaSalvar.getDocumentos().add(doc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        projetoService.salvar(projetoParaSalvar);
        return "redirect:/projetos";
    }

    @GetMapping("/deletar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletarProjeto(@PathVariable Long id) {
        projetoService.deletarPorId(id);
        return "redirect:/projetos";
    }
}