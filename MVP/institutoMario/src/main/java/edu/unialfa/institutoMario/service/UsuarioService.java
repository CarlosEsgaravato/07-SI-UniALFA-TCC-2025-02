package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long id = Long.parseLong(username);
            return usuarioRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário com ID " + id + " não encontrado"));
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("ID inválido: " + username);
        }
    }

    @Transactional
    public void salvar(Usuario usuario) {
        if (usuario.getId() != null) {
            Usuario original = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                usuario.setSenha(original.getSenha());
            } else {
                usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }

        } else {
            if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                throw new IllegalArgumentException("Senha é obrigatória no cadastro.");
            }

            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).get();
    }

    public void deletarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Usuario) auth.getPrincipal();
    }
}
