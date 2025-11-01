package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.dto.EventoDto;
import edu.unialfa.institutoMario.model.Evento;
import edu.unialfa.institutoMario.repository.EventoRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class EventoService {

    // Injeção via construtor (@AllArgsConstructor)
    private final EventoRepository repository;

    @Transactional
    public void salvar(Evento evento){
        repository.save(evento);
    }

    public List<Evento> listarTodos(){
        return repository.findAll();
    }

    public Evento buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(()-> new RuntimeException("Evento não encontrado"));
    }

    public void deletarPorId(Long id){
        if (!repository.existsById(id)){
            throw new RuntimeException("Evento com id " + id + " não encontrado para exclusão.");
        }
        repository.deleteById(id);
    }

    public boolean existeEventoNoHorario(LocalDateTime dataHora, Long idEvento) {
        if (idEvento == null) {
            return repository.existsByData(dataHora);
        } else {
            return repository.existsByDataAndIdNot(dataHora, idEvento);
        }
    }


    public List<Evento> buscarProximosEventos(int limite) {

        return repository.findByDataAfterOrderByDataAsc(
                LocalDateTime.now(),
                PageRequest.of(0, limite)
        );
    }
}