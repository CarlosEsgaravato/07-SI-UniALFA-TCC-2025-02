package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

}
