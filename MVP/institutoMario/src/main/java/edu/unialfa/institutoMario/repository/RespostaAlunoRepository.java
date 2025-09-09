package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.RespostaAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RespostaAlunoRepository extends JpaRepository<RespostaAluno, Long> {
    List<RespostaAluno> findByAlunoId(Long alunoId);
    List<RespostaAluno> findByProvaId(Long provaId);
}
