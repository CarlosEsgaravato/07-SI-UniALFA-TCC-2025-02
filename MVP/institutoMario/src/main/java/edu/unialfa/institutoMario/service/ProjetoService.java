package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Projetos;
import edu.unialfa.institutoMario.repository.ProjetoRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    @Transactional
    public void salvar(Projetos projeto){
        projetoRepository.save(projeto);
    }

    public List<Projetos> listarTodos(){
        return projetoRepository.findAll();
    }

    public Projetos bucarPorId(Long id){
        return projetoRepository.findById(id).orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
    }

    @Transactional
    public void deletarPorId(Long id) {
        if (!projetoRepository.existsById(id)) {
            throw new RuntimeException("Projeto com id " + id + " não encontrado para exclusão.");
        }
        projetoRepository.deleteById(id);
    }
}
