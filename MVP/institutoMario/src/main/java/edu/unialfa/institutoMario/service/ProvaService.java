package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.repository.ProvaRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProvaService {
    private final ProvaRepository repository;

    @Transactional
    public void salvar(Prova prova) {
        repository.save(prova);
    }

    public List<Prova> listarTodos() {
        return repository.findAll();
    }

    public Prova buscarPorId(Long id) {
        return repository.findById(id).get();
    }

    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }

    public List<Prova> listarPorProfessor(Long usuarioId) {
        return repository.findByDisciplina_Professor_Usuario_Id(usuarioId);
    }

    public List<Prova> listarPorDisciplina(Long idDisciplina) {
        return repository.findByDisciplinaId(idDisciplina);
    }
}

