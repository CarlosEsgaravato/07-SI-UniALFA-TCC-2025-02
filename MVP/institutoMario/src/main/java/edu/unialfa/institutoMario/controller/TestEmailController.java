package edu.unialfa.institutoMario.controller;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestEmailController {

    private final JavaMailSender mailSender;

    @GetMapping("/test-email")
    public String testarEmail(@RequestParam String destino) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destino);
            message.setSubject("Teste de Envio - Instituto Mário Gazin");
            message.setText(
                    "Este é um e-mail de teste!\n\n" +
                            "Se você recebeu esta mensagem, o sistema de envio está funcionando corretamente.\n\n" +
                            "Atenciosamente,\n" +
                            "Sistema Instituto Mário Gazin"
            );

            mailSender.send(message);

            return "✅ E-mail enviado com sucesso para: " + destino;
        } catch (Exception e) {
            return "❌ Erro ao enviar e-mail: " + e.getMessage();
        }
    }
}