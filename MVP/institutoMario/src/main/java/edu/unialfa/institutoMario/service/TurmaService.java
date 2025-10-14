package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.repository.TurmaRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final AlunoService alunoService; // INJEÇÃO ADICIONADA AQUI

    public List<Turma> listarTodas() {
        return turmaRepository.findAll();
    }

    public Turma buscarPorId(Long id) {
        return turmaRepository.findById(id).orElse(null);
    }

    @Transactional
    public void salvar(Turma turma) {
        turmaRepository.save(turma);
    }

    public void deletarPorId(Long id) {
        turmaRepository.deleteById(id);
    }

    public List<Disciplina> listarDisciplinasPorTurma(Long turmaId) {
        return turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"))
                .getDisciplinas();
    }

    public long contarTodasTurmas() {
        return turmaRepository.count();
    }

    public String buscarNomeDaTurmaDoAluno(Long alunoId) {
        return alunoService.buscarTurmaDoAluno(alunoId)
                .map(Turma::getNome)
                .orElse("Turma Não Definida");
    }
}