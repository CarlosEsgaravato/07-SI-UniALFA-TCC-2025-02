package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.dto.CorrecaoProvaRequest;
import edu.unialfa.institutoMario.dto.RespostaSimplesDTO;
import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Questao;
import edu.unialfa.institutoMario.model.RespostaAluno;
import edu.unialfa.institutoMario.repository.QuestaoRepository;
import edu.unialfa.institutoMario.repository.RespostaAlunoRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RespostaAlunoService {
    final private RespostaAlunoRepository respostaAlunoRepository;
    private final QuestaoRepository questaoRepository;
    private final AlunoService alunoService;

    public List<RespostaAluno> listarTodas() {
        return respostaAlunoRepository.findAll();
    }
    public RespostaAluno buscarPorId(Long id) {
        return respostaAlunoRepository.findById(id).orElse(null);
    }
    @Transactional
    public void salvar(RespostaAluno respostaAluno) {
        respostaAlunoRepository.save(respostaAluno);
    }
    public void deletarPorId(Long id) {
        respostaAlunoRepository.deleteById(id);
    }

    @Transactional
    public void processarRespostas(CorrecaoProvaRequest request) {
        Long idProva = request.getIdProva();
        Long idAluno = request.getIdAluno();

        Aluno alunoEntity = alunoService.buscarPorId(idAluno);

        boolean jaRespondeu = respostaAlunoRepository.existsByProvaIdAndAlunoId(idProva, idAluno);
        if (jaRespondeu) {
            System.out.println("Atualizando respostas do Aluno ID: " + idAluno + " para a Prova ID: " + idProva);
            respostaAlunoRepository.deleteByProvaIdAndAlunoId(idProva, idAluno);
        }


        for (RespostaSimplesDTO respostaDto : request.getRespostas()) {
            String numeroQuestaoStr = respostaDto.getNumeroQuestao();

            RespostaAluno respostaAluno = new RespostaAluno();
            respostaAluno.setAluno(alunoEntity);

            Optional<Questao> questaoOpt = questaoRepository.findByProvaIdAndNumero(idProva, numeroQuestaoStr);

            if (questaoOpt.isPresent()) {
                Questao questao = questaoOpt.get();
                respostaAluno.setProva(questao.getProva());

                respostaAluno.setNumeroQuestao(numeroQuestaoStr);

                respostaAluno.setAlternativaEscolhida(respostaDto.getAlternativaEscolhida());

                boolean correta = questao.getAlternativaCorreta().equalsIgnoreCase(respostaDto.getAlternativaEscolhida());
                respostaAluno.setCorreta(correta);

                respostaAlunoRepository.save(respostaAluno);
            } else {
                System.out.println("AVISO: Questão número " + numeroQuestaoStr + " não encontrada para a prova ID " + idProva);
            }
        }
    }

    public List<RespostaAluno> buscarPorAluno(Long alunoId) {
        return respostaAlunoRepository.findByAlunoId(alunoId);
    }

    public List<RespostaAluno> buscarPorAlunoEProva(Long alunoId, Long provaId) {
        return respostaAlunoRepository.findByAlunoIdAndProvaId(alunoId, provaId);
    }

    public List<Long> buscarIdsAlunosPorProva(Long provaId) {
        return respostaAlunoRepository.findAlunoIdsByProvaId(provaId);
    }

    public List<Long> buscarIdsDeProvasRespondidas(Long alunoId) {
        return respostaAlunoRepository.findProvasRespondidasIdsByAlunoId(alunoId);
    }
}