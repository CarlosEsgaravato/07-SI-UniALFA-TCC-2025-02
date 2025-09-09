package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.AlunoRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AlunoService {
    private final AlunoRepository repository;

    @Transactional
    public void salvar(Aluno aluno) {
        repository.save(aluno);
    }

    public List<Aluno> listarTodos() {
        return repository.findAll();
    }

    public Aluno buscarPorId(Long id) {
        return repository.findById(id).get();
    }

    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }

    public boolean existsByUsuarioId(Long usuarioId) {
        return repository.existsByUsuarioId(usuarioId);
    }

    public void deletarPorUsuarioId(Long usuarioId) {
        repository.findByUsuarioId(usuarioId)
                .ifPresent(repository::delete);
    }

    public List<Aluno> listarAlunosSemTurma() {
        return repository.findByTurmaIsNull();
    }

    public List<Aluno> listarPorTurma(Turma turma) {
        return repository.findByTurmaId(turma.getId());
    }

    public Aluno buscarPorUsuario(Usuario usuario) {
        return repository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado para o usuário logado"));
    }
}
