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
            TipoUsuario tipoAluno = tipoUsuarioRepository.findByDescricao("ALUNO");
            if (tipoAluno == null) {
                tipoAluno = new TipoUsuario();
                tipoAluno.setDescricao("ALUNO");
                tipoAluno = tipoUsuarioRepository.save(tipoAluno);
                System.out.println("Tipo de usuário 'ALUNO' inserido com sucesso.");
            }
            TipoUsuario tipoProfessor = tipoUsuarioRepository.findByDescricao("PROFESSOR");
            if (tipoProfessor == null) {
                tipoProfessor = new TipoUsuario();
                tipoProfessor.setDescricao("PROFESSOR");
                tipoProfessor = tipoUsuarioRepository.save(tipoProfessor);
                System.out.println("Tipo de usuário 'PROFESSOR' inserido com sucesso.");
            }
            if (usuarioRepository.findByCpf("12345678900") == null) {
                Usuario admin = new Usuario();
                admin.setNome("Admin");
                admin.setCpf("12345678900");
                admin.setEmail("admin@unialfa.edu");
                admin.setTelefone("62999999999");
                admin.setTipoUsuario(tipoAdmin);
                admin.setSenha(passwordEncoder.encode("123456")); // Senha original do admin
                usuarioRepository.save(admin);
                System.out.println("Usuário 'admin' inserido com sucesso.");
            } else {
                System.out.println("Usuário 'admin' já existe.");
            }
            if (usuarioRepository.findByCpf("11122233344") == null) {
                Usuario aluno = new Usuario();
                aluno.setNome("Aluno Padrão");
                aluno.setCpf("11122233344");
                aluno.setEmail("aluno@unialfa.edu");
                aluno.setTelefone("62988888888");
                aluno.setTipoUsuario(tipoAluno);
                aluno.setSenha(passwordEncoder.encode("12345")); // Senha padrão solicitada
                usuarioRepository.save(aluno);
                System.out.println("Usuário 'aluno' inserido com sucesso.");
            } else {
                System.out.println("Usuário 'aluno' já existe.");
            }
            if (usuarioRepository.findByCpf("55566677788") == null) {
                Usuario professor = new Usuario();
                professor.setNome("Professor Padrão");
                professor.setCpf("55566677788");
                professor.setEmail("professor@unialfa.edu");
                professor.setTelefone("62977777777");
                professor.setTipoUsuario(tipoProfessor);
                professor.setSenha(passwordEncoder.encode("12345")); // Senha padrão solicitada
                usuarioRepository.save(professor);
                System.out.println("Usuário 'professor' inserido com sucesso.");
            } else {
                System.out.println("Usuário 'professor' já existe.");
            }
        };
    }
}