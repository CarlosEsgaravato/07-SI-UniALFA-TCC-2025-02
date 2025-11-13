package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Evento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByDataAfterOrderByDataAsc(LocalDateTime data, Pageable pageable);
    List<Evento> findByDataBetweenOrderByDataAsc(LocalDateTime dataInicio, LocalDateTime dataFim);
}

