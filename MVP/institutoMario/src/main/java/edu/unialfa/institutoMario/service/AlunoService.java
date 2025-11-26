package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.AlunoRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor; // MANTIDO COMO PEDISTE
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));
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
        return repository.findAll().stream()
                .filter(a -> a.getTurmas().isEmpty())
                .collect(Collectors.toList());
    }

    public List<Aluno> listarPorTurma(Turma turma) {
        return repository.findAll().stream()
                .filter(a -> a.getTurmas().contains(turma))
                .collect(Collectors.toList());
    }

    public Optional<Turma> buscarTurmaDoAluno(Long alunoId) {
        return repository.findById(alunoId)
                .map(aluno -> {
                    if (!aluno.getTurmas().isEmpty()) {
                        return aluno.getTurmas().get(0);
                    }
                    return null;
                });
    }

    public Aluno buscarPorUsuario(Usuario usuario) {
        return repository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado para o usuário logado"));
    }

    public long contarTodosAlunos() {
        return repository.count();
    }

    public Aluno buscarPorUsuarioId(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).orElse(null);
    }

    public List<Aluno> listarAlunosPorTurmaId(Long turmaId) {
        if (turmaId == null || turmaId == 0) return java.util.Collections.emptyList();

        return repository.findAll().stream()
                .filter(a -> a.getTurmas().stream().anyMatch(t -> t.getId().equals(turmaId)))
                .collect(Collectors.toList());
    }
}