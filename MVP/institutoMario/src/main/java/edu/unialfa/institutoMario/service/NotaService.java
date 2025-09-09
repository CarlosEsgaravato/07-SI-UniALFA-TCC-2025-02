package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.dto.NotaDTO;
import edu.unialfa.institutoMario.dto.NotaPorDisciplinaDTO;
import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.model.Questao;
import edu.unialfa.institutoMario.model.RespostaAluno;
import edu.unialfa.institutoMario.repository.ProvaRepository;
import edu.unialfa.institutoMario.repository.RespostaAlunoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotaService {

    private final RespostaAlunoRepository respostaAlunoRepository;
    private final ProvaRepository provaRepository;

    public List<NotaPorDisciplinaDTO> buscarNotasDoAlunoAgrupadas(Long alunoId) {
        List<RespostaAluno> respostas = respostaAlunoRepository.findByAlunoId(alunoId);

        Map<Prova, List<RespostaAluno>> agrupadasPorProva = respostas.stream()
                .collect(Collectors.groupingBy(RespostaAluno::getProva));

        List<NotaDTO> notas = new ArrayList<>();
        for (Map.Entry<Prova, List<RespostaAluno>> entry : agrupadasPorProva.entrySet()) {
            Prova prova = entry.getKey();
            List<RespostaAluno> respostasDaProva = entry.getValue();

            BigDecimal nota = respostasDaProva.stream()
                    .filter(RespostaAluno::isCorreta)
                    .map(resposta -> {
                        Optional<Questao> questao = prova.getQuestoes().stream()
                                .filter(q -> q.getNumero().equals(resposta.getNumeroQuestao()))
                                .findFirst();
                        return questao.map(Questao::getPontuacao).orElse(BigDecimal.ZERO);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            NotaDTO dto = new NotaDTO();
            dto.setAlunoNome(respostasDaProva.getFirst().getAluno().getUsuario().getNome());
            dto.setDisciplinaNome(prova.getDisciplina().getNome());
            dto.setData(prova.getData());
            dto.setNotaTotal(nota);
            notas.add(dto);
        }

        return notas.stream()
                .collect(Collectors.groupingBy(NotaDTO::getDisciplinaNome))
                .entrySet().stream()
                .map(entry -> new NotaPorDisciplinaDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<NotaPorDisciplinaDTO> buscarNotasDosAlunosPorProfessorAgrupadas(Long professorId) {
        List<Prova> provas = provaRepository.findByDisciplina_Professor_Id(professorId);

        List<NotaDTO> notas = new ArrayList<>();
        for (Prova prova : provas) {
            List<RespostaAluno> respostas = respostaAlunoRepository.findByProvaId(prova.getId());
            Map<Aluno, List<RespostaAluno>> respostasPorAluno = respostas.stream()
                    .collect(Collectors.groupingBy(RespostaAluno::getAluno));

            for (Map.Entry<Aluno, List<RespostaAluno>> entry : respostasPorAluno.entrySet()) {
                Aluno aluno = entry.getKey();
                List<RespostaAluno> respostasDoAluno = entry.getValue();

                BigDecimal nota = respostasDoAluno.stream()
                        .filter(RespostaAluno::isCorreta)
                        .map(resposta -> {
                            Optional<Questao> questao = prova.getQuestoes().stream()
                                    .filter(q -> q.getNumero().equals(resposta.getNumeroQuestao()))
                                    .findFirst();
                            return questao.map(Questao::getPontuacao).orElse(BigDecimal.ZERO);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                NotaDTO dto = new NotaDTO();
                dto.setAlunoNome(aluno.getUsuario().getNome());
                dto.setDisciplinaNome(prova.getDisciplina().getNome());
                dto.setData(prova.getData());
                dto.setNotaTotal(nota);
                notas.add(dto);
            }
        }

        return notas.stream()
                .collect(Collectors.groupingBy(NotaDTO::getDisciplinaNome))
                .entrySet().stream()
                .map(entry -> new NotaPorDisciplinaDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
