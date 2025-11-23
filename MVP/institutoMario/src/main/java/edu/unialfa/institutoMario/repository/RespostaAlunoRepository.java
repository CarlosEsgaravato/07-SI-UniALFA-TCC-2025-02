package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.RespostaAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RespostaAlunoRepository extends JpaRepository<RespostaAluno, Long> {
    List<RespostaAluno> findByAlunoId(Long alunoId);
    List<RespostaAluno> findByProvaId(Long provaId);
    boolean existsByProvaIdAndAlunoId(Long provaId, Long alunoId);

    @Query("SELECT DISTINCT ra.prova.id FROM RespostaAluno ra WHERE ra.aluno.id = :alunoId")
    List<Long> findProvasRespondidasIdsByAlunoId(Long alunoId);
    void deleteByProvaIdAndAlunoId(Long provaId, Long alunoId);
    List<RespostaAluno> findByAlunoIdAndProvaId(Long alunoId, Long provaId);

    @Query("SELECT DISTINCT r.aluno.id FROM RespostaAluno r WHERE r.prova.id = :provaId")
    List<Long> findAlunoIdsByProvaId(@Param("provaId") Long provaId);
    boolean existsByProvaId(Long provaId);
}
