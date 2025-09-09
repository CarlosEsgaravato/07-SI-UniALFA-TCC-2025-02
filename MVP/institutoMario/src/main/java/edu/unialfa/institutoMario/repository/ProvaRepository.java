package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Prova;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvaRepository extends JpaRepository<Prova, Long> {
    List<Prova> findByDisciplina_Professor_Usuario_Id(Long usuarioId);
    List<Prova> findByDisciplinaId(Long idDisciplina);
    List<Prova> findByDisciplina_Professor_Id(Long professorId);
}
