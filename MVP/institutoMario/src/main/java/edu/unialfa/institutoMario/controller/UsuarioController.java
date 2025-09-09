package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Professor;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.service.AlunoService;
import edu.unialfa.institutoMario.service.ProfessorService;
import edu.unialfa.institutoMario.service.TipoUsuarioService;
import edu.unialfa.institutoMario.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final TipoUsuarioService tipoUsuarioService;
    private final ProfessorService professorService;
    private final AlunoService alunoService;

    @GetMapping
    public String listar(Usuario usuario, Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());

        return "usuarios/lista";
    }

    @GetMapping("cadastrar")
    public String cadastrar(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tipos", tipoUsuarioService.listarTodos());

        return "usuarios/form";
    }

    @PostMapping
    public String salvar(Usuario usuario, Model model) {
        try {
            usuarioService.salvar(usuario);

            Long tipoId = usuario.getTipoUsuario().getId();

            if (tipoId == 2) {
                if (alunoService.existsByUsuarioId(usuario.getId())) {
                    alunoService.deletarPorUsuarioId(usuario.getId());
                }

                if (!professorService.existsByUsuarioId(usuario.getId())) {
                    Professor professor = new Professor();
                    professor.setUsuario(usuario);
                    professorService.salvar(professor);
                }

            } else if (tipoId == 3) {
                if (professorService.existsByUsuarioId(usuario.getId())) {
                    professorService.deletarPorUsuarioId(usuario.getId());
                }

                if (!alunoService.existsByUsuarioId(usuario.getId())) {
                    Aluno aluno = new Aluno();
                    aluno.setUsuario(usuario);
                    alunoService.salvar(aluno);
                }

            } else {
                alunoService.deletarPorUsuarioId(usuario.getId());
                professorService.deletarPorUsuarioId(usuario.getId());
            }

            return "redirect:/usuarios";

        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            return listar(usuario, model);
        }
    }


    @GetMapping("editar/{id}")
    public String atualizar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tipos", tipoUsuarioService.listarTodos());

        return "usuarios/form";
    }

    @GetMapping("deletar/{id}")
    public String deletar(@PathVariable Long id, Model model) {
        if (professorService.existsByUsuarioId(id)) {
            professorService.deletarPorUsuarioId(id);
        }

        if (alunoService.existsByUsuarioId(id)) {
            alunoService.deletarPorUsuarioId(id);
        }

        usuarioService.deletarPorId(id);

        return "redirect:/usuarios";
    }
}
