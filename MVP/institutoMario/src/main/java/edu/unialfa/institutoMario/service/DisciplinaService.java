package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Professor;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.repository.DisciplinaRepository;
import edu.unialfa.institutoMario.repository.ProfessorRepository;
import edu.unialfa.institutoMario.repository.TurmaRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;

    public List<Disciplina> listarTodas() {
        return disciplinaRepository.findAll();
    }

    public Disciplina buscarPorId(Long id) {
        return disciplinaRepository.findById(id).orElse(null);
    }

    @Transactional
    public void salvar(Disciplina disciplina) {
        Turma turma = turmaRepository.findById(disciplina.getTurma().getId()).orElseThrow();
        Professor professor = professorRepository.findById(disciplina.getProfessor().getId()).orElseThrow();
        disciplina.setTurma(turma);
        disciplina.setProfessor(professor);
        disciplinaRepository.save(disciplina);
    }

    public void deletarPorId(Long id) {
        disciplinaRepository.deleteById(id);
    }

    public List<Disciplina> listarPorProfessor(Long usuarioId) {
        return disciplinaRepository.findByProfessor_Usuario_Id(usuarioId);
    }

    public List<Disciplina> listarPorAluno(Long usuarioId) {
        return disciplinaRepository.findAllByTurma_Alunos_Usuario_Id(usuarioId);
    }

}