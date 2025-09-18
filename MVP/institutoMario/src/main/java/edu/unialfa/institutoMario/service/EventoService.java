package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.dto.EventoDto;
import edu.unialfa.institutoMario.model.Evento;
import edu.unialfa.institutoMario.repository.EventoRepository;
import edu.unialfa.institutoMario.repository.TurmaRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EventoService {

    @Autowired
    private EventoRepository repository;

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
}
