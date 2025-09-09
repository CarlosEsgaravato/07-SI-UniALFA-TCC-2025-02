package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.TipoUsuario;
import edu.unialfa.institutoMario.repository.TipoUsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TipoUsuarioService {
    private final TipoUsuarioRepository repository;

    @Transactional
    public void salvar(TipoUsuario tipoUsuario) {
        repository.save(tipoUsuario);
    }

    public List<TipoUsuario> listarTodos() {
        return repository.findAll();
    }

    public TipoUsuario buscarPorId(Long id) {
        return repository.findById(id).get();
    }

    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }
}
