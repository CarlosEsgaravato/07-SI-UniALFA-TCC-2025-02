package edu.unialfa.institutoMario.config;

import edu.unialfa.institutoMario.model.TipoUsuario;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.TipoUsuarioRepository;
import edu.unialfa.institutoMario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoaderConfig {

    @Bean
    public CommandLineRunner initDatabase(
            UsuarioRepository usuarioRepository,
            TipoUsuarioRepository tipoUsuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            TipoUsuario tipoAdmin = tipoUsuarioRepository.findByDescricao("ADMIN");
            if (tipoAdmin == null) {
                tipoAdmin = new TipoUsuario();
                tipoAdmin.setDescricao("ADMIN");
                tipoAdmin = tipoUsuarioRepository.save(tipoAdmin);
                System.out.println("Tipo de usuário 'ADMIN' inserido com sucesso.");
            }

            // Verifique se o usuário 'admin' já existe pelo CPF ou email
            Usuario usuarioExistente = usuarioRepository.findByCpf("12345678900");
            if (usuarioExistente == null) {
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome("Admin");
                novoUsuario.setCpf("12345678900");
                novoUsuario.setEmail("admin@unialfa.edu");
                novoUsuario.setTelefone("62999999999");
                novoUsuario.setTipoUsuario(tipoAdmin);
                novoUsuario.setSenha(passwordEncoder.encode("123456"));

                usuarioRepository.save(novoUsuario);
                System.out.println("Usuário 'admin' inserido com sucesso.");
            } else {
                System.out.println("Usuário 'admin' já existe. Nenhum novo usuário foi inserido.");
            }
        };
    }
}