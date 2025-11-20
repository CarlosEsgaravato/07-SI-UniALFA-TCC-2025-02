package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.service.PasswordRecoveryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@AllArgsConstructor
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;


    @GetMapping("/esqueci-senha")
    public String exibirPaginaEsqueciSenha() {
        return "esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String processarEsqueciSenha(
            @RequestParam("email") String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        // Constrói URL base da aplicação
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }
        baseUrl += request.getContextPath();

        boolean emailEnviado = passwordRecoveryService.solicitarRecuperacaoSenha(email, baseUrl);

        if (emailEnviado) {
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Um link de recuperação foi enviado para o seu e-mail. Verifique sua caixa de entrada.");
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "E-mail não encontrado em nossa base de dados.");
        }

        return "redirect:/esqueci-senha";
    }


    @GetMapping("/recuperar-senha")
    public String exibirPaginaRedefinirSenha(
            @RequestParam("token") String token,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Usuario> usuarioOpt = passwordRecoveryService.validarToken(token);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Link de recuperação inválido ou expirado. Solicite um novo link.");
            return "redirect:/esqueci-senha";
        }

        model.addAttribute("token", token);
        return "redefinir-senha";
    }

    @PostMapping("/recuperar-senha")
    public String processarRedefinirSenha(
            @RequestParam("token") String token,
            @RequestParam("novaSenha") String novaSenha,
            @RequestParam("confirmarSenha") String confirmarSenha,
            RedirectAttributes redirectAttributes
    ) {
        // Valida se as senhas coincidem
        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "As senhas não coincidem. Por favor, tente novamente.");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/recuperar-senha";
        }

        // Valida tamanho mínimo da senha
        if (novaSenha.length() < 6) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "A senha deve ter no mínimo 6 caracteres.");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/recuperar-senha";
        }

        boolean senhaRedefinida = passwordRecoveryService.redefinirSenha(token, novaSenha);

        if (senhaRedefinida) {
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Senha alterada com sucesso! Você já pode fazer login com sua nova senha.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Link de recuperação inválido ou expirado. Solicite um novo link.");
            return "redirect:/esqueci-senha";
        }
    }
}