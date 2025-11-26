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
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotaService {

    private final RespostaAlunoRepository respostaAlunoRepository;
    private final ProvaRepository provaRepository;
    private final TurmaService turmaService;
    public String calcularMediaGeral() {
        List<RespostaAluno> todasRespostas = respostaAlunoRepository.findAll();

        if (todasRespostas.isEmpty()) {
            return "—";
        }
        Map<Prova, Map<Aluno, List<RespostaAluno>>> notasAgrupadas = todasRespostas.stream()
                .collect(Collectors.groupingBy(RespostaAluno::getProva,
                        Collectors.groupingBy(RespostaAluno::getAluno)));

        List<BigDecimal> notasFinais = new ArrayList<>();

        for (Map.Entry<Prova, Map<Aluno, List<RespostaAluno>>> provaEntry : notasAgrupadas.entrySet()) {
            Prova prova = provaEntry.getKey();

            for (Map.Entry<Aluno, List<RespostaAluno>> alunoEntry : provaEntry.getValue().entrySet()) {
                List<RespostaAluno> respostasDoAluno = alunoEntry.getValue();
                BigDecimal nota = respostasDoAluno.stream()
                        .filter(RespostaAluno::isCorreta)
                        .map(resposta -> {
                            Optional<Questao> questao = prova.getQuestoes().stream()
                                    .filter(q -> q.getNumero().equals(resposta.getNumeroQuestao()))
                                    .findFirst();
                            return questao.map(Questao::getPontuacao).orElse(BigDecimal.ZERO);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                notasFinais.add(nota);
            }
        }

        if (notasFinais.isEmpty()) {
            return "—";
        }

        BigDecimal somaTotal = notasFinais.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal count = BigDecimal.valueOf(notasFinais.size());

        BigDecimal media = somaTotal.divide(count, 2, RoundingMode.HALF_UP);

        return media.setScale(1, RoundingMode.HALF_UP).toString();
    }


    public Map<String, String> calcularMediaPorTurma() {
        List<RespostaAluno> todasRespostas = respostaAlunoRepository.findAll();

        Map<Prova, Map<Aluno, BigDecimal>> notasPorProvaAluno = todasRespostas.stream()
                .collect(Collectors.groupingBy(RespostaAluno::getProva))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        provaEntry -> provaEntry.getValue().stream()
                                .collect(Collectors.groupingBy(RespostaAluno::getAluno))
                                .entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        alunoEntry -> alunoEntry.getValue().stream()
                                                .filter(RespostaAluno::isCorreta)
                                                .map(resposta -> {
                                                    Optional<Questao> questao = provaEntry.getKey().getQuestoes().stream()
                                                            .filter(q -> q.getNumero().equals(resposta.getNumeroQuestao()))
                                                            .findFirst();
                                                    return questao.map(Questao::getPontuacao).orElse(BigDecimal.ZERO);
                                                })
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                ))
                ));


        Map<String, List<BigDecimal>> notasAgrupadasPorTurma = new HashMap<>();

        for (Map.Entry<Prova, Map<Aluno, BigDecimal>> provaEntry : notasPorProvaAluno.entrySet()) {
            for (Map.Entry<Aluno, BigDecimal> alunoEntry : provaEntry.getValue().entrySet()) {
                Aluno aluno = alunoEntry.getKey();
                BigDecimal nota = alunoEntry.getValue();
                String nomeTurma = turmaService.buscarNomeDaTurmaDoAluno(aluno.getId());

                notasAgrupadasPorTurma
                        .computeIfAbsent(nomeTurma, k -> new ArrayList<>())
                        .add(nota);
            }
        }

        Map<String, String> mediaPorTurmaFormatada = notasAgrupadasPorTurma.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            BigDecimal soma = entry.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal count = BigDecimal.valueOf(entry.getValue().size());

                            if (count.compareTo(BigDecimal.ZERO) == 0) return "—";

                            BigDecimal media = soma.divide(count, 2, RoundingMode.HALF_UP);
                            return media.setScale(1, RoundingMode.HALF_UP).toString();
                        }
                ));

        return mediaPorTurmaFormatada;
    }


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