package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    boolean existsByUsuarioId(Long usuarioId);
    Optional<Aluno> findByUsuarioId(Long usuarioId);
    List<Aluno> findByTurmaIsNull();
    List<Aluno> findByTurmaId(Long turmaId);
    Optional<Aluno> findByUsuario(Usuario usuario);
}
