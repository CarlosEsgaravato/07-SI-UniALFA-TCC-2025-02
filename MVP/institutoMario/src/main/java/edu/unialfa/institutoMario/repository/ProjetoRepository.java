package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Projetos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoRepository extends JpaRepository<Projetos, Long> {
}