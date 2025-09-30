package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.RespostaAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RespostaAlunoRepository extends JpaRepository<RespostaAluno, Long> {
    List<RespostaAluno> findByAlunoId(Long alunoId);
    List<RespostaAluno> findByProvaId(Long provaId);
    boolean existsByProvaIdAndAlunoId(Long provaId, Long alunoId);
    @Query("SELECT DISTINCT ra.prova.id FROM RespostaAluno ra WHERE ra.aluno.id = :alunoId")
    List<Long> findProvasRespondidasIdsByAlunoId(Long alunoId);
}
