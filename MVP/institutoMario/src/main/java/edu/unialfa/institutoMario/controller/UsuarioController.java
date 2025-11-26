package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Professor;
import edu.unialfa.institutoMario.model.Responsavel;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.ResponsavelRepository;
import edu.unialfa.institutoMario.service.AlunoService;
import edu.unialfa.institutoMario.service.ProfessorService;
import edu.unialfa.institutoMario.service.TipoUsuarioService;
import edu.unialfa.institutoMario.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

@Controller
@AllArgsConstructor
@RequestMapping("usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final TipoUsuarioService tipoUsuarioService;
    private final ProfessorService professorService;
    private final AlunoService alunoService;
    private final ResponsavelRepository responsavelRepository; // Injeção nova

    @InitBinder("responsavel")
    public void initBinder(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("responsavel.");
    }

    @GetMapping
    public String listar(Usuario usuario, Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("cadastrar")
    public String cadastrar(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("responsavel", new Responsavel()); // Enviar objeto vazio
        model.addAttribute("tipos", tipoUsuarioService.listarTodos());
        return "usuarios/form";
    }

    @PostMapping
    public String salvar(Usuario usuario,
                         Model model,
                         @RequestParam(required = false) Long respId,
                         @RequestParam(required = false) String respNome,
                         @RequestParam(required = false) String respCpf,
                         @RequestParam(required = false) String respTelefone,
                         @RequestParam(required = false) String respEmail) {

        boolean emailJaExiste = usuarioService.existsByEmailAndIdNot(usuario.getEmail(), usuario.getId());
        boolean cpfJaExiste = usuarioService.existsByCpfAndIdNot(usuario.getCpf(), usuario.getId());

        String erroResponsavel = "";
        Long tipoId = usuario.getTipoUsuario().getId();

        if (tipoId == 3) { // Aluno
            if (respNome == null || respNome.isEmpty()) erroResponsavel += "Nome do responsável é obrigatório. ";
            if (respCpf == null || respCpf.isEmpty()) erroResponsavel += "CPF do responsável é obrigatório. ";
            if (respTelefone == null || respTelefone.isEmpty()) erroResponsavel += "Telefone do responsável é obrigatório. ";
            if (respEmail == null || respEmail.isEmpty()) erroResponsavel += "E-mail do responsável é obrigatório. ";
        }

        if (emailJaExiste || cpfJaExiste || !erroResponsavel.isEmpty()) {
            String mensagemErro = "";
            if (emailJaExiste) mensagemErro += "O E-mail já existe. ";
            if (cpfJaExiste) mensagemErro += "O CPF já existe. ";
            mensagemErro += erroResponsavel;

            model.addAttribute("erroGeral", mensagemErro.trim());
            model.addAttribute("tipos", tipoUsuarioService.listarTodos());


            Responsavel respTemp = new Responsavel();
            respTemp.setId(respId);
            respTemp.setNome(respNome);
            respTemp.setCpf(respCpf);
            respTemp.setTelefone(respTelefone);
            respTemp.setEmail(respEmail);
            model.addAttribute("responsavel", respTemp);

            return "usuarios/form";
        }

        try {
            usuarioService.salvar(usuario);

            if (tipoId == 2) { // Professor
                if (alunoService.existsByUsuarioId(usuario.getId())) {
                    alunoService.deletarPorUsuarioId(usuario.getId());
                }
                if (!professorService.existsByUsuarioId(usuario.getId())) {
                    Professor professor = new Professor();
                    professor.setUsuario(usuario);
                    professorService.salvar(professor);
                }
            } else if (tipoId == 3) { // Aluno
                if (professorService.existsByUsuarioId(usuario.getId())) {
                    professorService.deletarPorUsuarioId(usuario.getId());
                }

                Aluno aluno = alunoService.buscarPorUsuarioId(usuario.getId());
                if (aluno == null) {
                    aluno = new Aluno();
                    aluno.setUsuario(usuario);
                }

                Responsavel responsavelParaSalvar;

                if (aluno.getResponsavel() != null) {
                    responsavelParaSalvar = aluno.getResponsavel();
                } else if (respId != null) {
                    responsavelParaSalvar = responsavelRepository.findById(respId).orElse(new Responsavel());
                } else {
                    responsavelParaSalvar = new Responsavel();
                }
                responsavelParaSalvar.setNome(respNome);
                responsavelParaSalvar.setCpf(respCpf);
                responsavelParaSalvar.setTelefone(respTelefone);
                responsavelParaSalvar.setEmail(respEmail);
                Responsavel responsavelSalvo = responsavelRepository.save(responsavelParaSalvar);
                aluno.setResponsavel(responsavelSalvo);
                alunoService.salvar(aluno);

            } else {
                alunoService.deletarPorUsuarioId(usuario.getId());
                professorService.deletarPorUsuarioId(usuario.getId());
            }
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("erroGeral", "Erro interno: " + e.getMessage());
            model.addAttribute("tipos", tipoUsuarioService.listarTodos());
            Responsavel respTemp = new Responsavel();
            respTemp.setNome(respNome);
            respTemp.setCpf(respCpf);
            respTemp.setTelefone(respTelefone);
            respTemp.setEmail(respEmail);
            model.addAttribute("responsavel", respTemp);

            return "usuarios/form";
        }
    }

    @GetMapping("editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        Aluno aluno = alunoService.buscarPorUsuarioId(id);
        if (aluno != null && aluno.getResponsavel() != null) {
            model.addAttribute("responsavel", aluno.getResponsavel());
        } else {
            model.addAttribute("responsavel", new Responsavel());
        }

        model.addAttribute("tipos", tipoUsuarioService.listarTodos());
        return "usuarios/form";
    }

    @GetMapping("deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.deletarPorId(id);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário deletado com sucesso!");
        }catch (DataIntegrityViolationException e){
            redirectAttributes.addFlashAttribute("erro", "Não foi possível excluir o usuário. Ele está vinculado a uma turma, projeto ou outro registro no sistema.");
        }catch (Exception e ){
            redirectAttributes.addFlashAttribute("erro", "Ocorreu um erro inesperado ao tentar excluir o usuário.");
        }
        return "redirect:/usuarios";
    }

    @ModelAttribute("usuarioLogado")
    public Usuario getUsuarioLogado() {
        return usuarioService.getUsuarioLogado();
    }
}