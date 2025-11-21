package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.dto.DadosAutorizacao;
import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Responsavel;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class AutorizacaoService {

    private final JavaMailSender mailSender;
    private final AlunoService alunoService;

    public void enviarEmailsAutorizacao(DadosAutorizacao dados) {
        List<Aluno> alunos = alunoService.listarAlunosPorTurmaId(dados.getTurmaId());

        int emailsEnviados = 0;

        for (Aluno aluno : alunos) {
            Responsavel responsavel = aluno.getResponsavel();
            if (responsavel != null && responsavel.getEmail() != null && !responsavel.getEmail().isEmpty()) {
                try {
                    enviarEmailIndividual(responsavel, aluno, dados);
                    emailsEnviados++;
                } catch (Exception e) {
                    System.err.println("Erro ao enviar e-mail para: " + responsavel.getEmail());
                }
            }
        }
        System.out.println("Total de e-mails de autorização enviados: " + emailsEnviados);
    }

    private void enviarEmailIndividual(Responsavel responsavel, Aluno aluno, DadosAutorizacao dados) {
        SimpleMailMessage message = new SimpleMailMessage();

        String dataFormatada = dados.getDataEvento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        message.setTo(responsavel.getEmail());
        message.setSubject("Autorização de Passeio/Evento: " + dados.getTituloEvento());

        String texto = String.format(
                "Olá, %s.\n\n" +
                        "Solicitamos autorização para o aluno(a) %s participar do seguinte evento:\n\n" +
                        "Evento: %s\n" +
                        "Local: %s\n" +
                        "Data: %s\n" +
                        "Saída: %s | Retorno: %s\n\n" +
                        "Observações: %s\n\n" +
                        "Por favor, responda a este e-mail ou assine a autorização impressa enviada pelo aluno.\n\n" +
                        "Atenciosamente,\nInstituto Mário Gazin",
                responsavel.getNome(),
                aluno.getUsuario().getNome(),
                dados.getTituloEvento(),
                dados.getLocal(),
                dataFormatada,
                dados.getHorarioSaida(),
                dados.getHorarioRetorno(),
                dados.getDescricao()
        );

        message.setText(texto);
        mailSender.send(message);
    }
}