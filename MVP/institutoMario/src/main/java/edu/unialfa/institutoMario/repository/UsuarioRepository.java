package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findById(Long id);

    Usuario findByCpf(String cpf);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    // ===== MÉTODOS PARA RECUPERAÇÃO DE SENHA =====

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByResetPasswordToken(String token);
}