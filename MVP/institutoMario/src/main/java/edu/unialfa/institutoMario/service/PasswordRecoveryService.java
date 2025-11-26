package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PasswordRecoveryService {

    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean solicitarRecuperacaoSenha(String email, String baseUrl) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();

        String token = UUID.randomUUID().toString();

        // Define token e expiração (1 hora)
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));

        usuarioRepository.save(usuario);

        enviarEmailRecuperacao(usuario.getEmail(), usuario.getNome(), token, baseUrl);

        return true;
    }

    private void enviarEmailRecuperacao(String email, String nome, String token, String baseUrl) {
        String linkRecuperacao = baseUrl + "/recuperar-senha?token=" + token;

        System.out.println("=== ENVIANDO E-MAIL DE RECUPERACAO ===");
        System.out.println("Destinatario: " + email);
        System.out.println("Link: " + linkRecuperacao);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Recuperacao de Senha - Instituto Mario Gazin");
        message.setText(
                "Ola, " + nome + "!\n\n" +
                        "Recebemos uma solicitacao para recuperacao de senha da sua conta no Sistema de Gestao do Instituto Mario Gazin.\n\n" +
                        "Para redefinir sua senha, clique no link abaixo:\n\n" +
                        linkRecuperacao + "\n\n" +
                        "Este link e valido por 1 hora.\n\n" +
                        "Se voce nao solicitou a recuperacao de senha, ignore este e-mail. Sua senha permanecera inalterada.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe Instituto Mario Gazin"
        );

        try {
            mailSender.send(message);
            System.out.println("✓ E-mail enviado com sucesso!");
        } catch (Exception e) {
            System.err.println("✗ Erro ao enviar e-mail: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao enviar e-mail de recuperacao", e);
        }
    }


    public Optional<Usuario> validarToken(String token) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetPasswordToken(token);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        return Optional.of(usuario);
    }


    @Transactional
    public boolean redefinirSenha(String token, String novaSenha) {
        Optional<Usuario> usuarioOpt = validarToken(token);

        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();

        usuario.setSenha(passwordEncoder.encode(novaSenha));

        // Limpa token (impede reutilização)
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiry(null);

        usuarioRepository.save(usuario);

        return true;
    }
}