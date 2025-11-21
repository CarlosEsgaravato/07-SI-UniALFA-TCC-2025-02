package edu.unialfa.institutoMario.config;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Responsavel;
import edu.unialfa.institutoMario.model.TipoUsuario;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.repository.AlunoRepository;
import edu.unialfa.institutoMario.repository.ResponsavelRepository;
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
            AlunoRepository alunoRepository,
            ResponsavelRepository responsavelRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            TipoUsuario tipoAdmin = tipoUsuarioRepository.findByDescricao("ADMIN");
            if (tipoAdmin == null) {
                tipoAdmin = new TipoUsuario();
                tipoAdmin.setDescricao("ADMIN");
                tipoAdmin = tipoUsuarioRepository.save(tipoAdmin);
                System.out.println("Tipo de usuário 'ADMIN' inserido com sucesso.");
            }
            TipoUsuario tipoProfessor = tipoUsuarioRepository.findByDescricao("PROFESSOR");
            if (tipoProfessor == null) {
                tipoProfessor = new TipoUsuario();
                tipoProfessor.setDescricao("PROFESSOR");
                tipoProfessor = tipoUsuarioRepository.save(tipoProfessor);
                System.out.println("Tipo de usuário 'PROFESSOR' inserido com sucesso.");
            }
            TipoUsuario tipoAluno = tipoUsuarioRepository.findByDescricao("ALUNO");
            if (tipoAluno == null) {
                tipoAluno = new TipoUsuario();
                tipoAluno.setDescricao("ALUNO");
                tipoAluno = tipoUsuarioRepository.save(tipoAluno);
                System.out.println("Tipo de usuário 'ALUNO' inserido com sucesso.");
            }
            if (usuarioRepository.findByCpf("12345678900") == null) {
                Usuario admin = new Usuario();
                admin.setNome("Admin");
                admin.setCpf("12345678900");
                admin.setEmail("admin@unialfa.edu");
                admin.setTelefone("62999999999");
                admin.setTipoUsuario(tipoAdmin);
                admin.setSenha(passwordEncoder.encode("123456"));
                usuarioRepository.save(admin);
                System.out.println("Usuário 'admin' inserido com sucesso.");
            } else {
                System.out.println("Usuário 'admin' já existe.");
            }
            if (usuarioRepository.findByCpf("11122233344") == null) {
                // a) Criar o Usuário (Login)
                Usuario usuarioAluno = new Usuario();
                usuarioAluno.setNome("Aluno Padrão");
                usuarioAluno.setCpf("11122233344");
                usuarioAluno.setEmail("aluno@unialfa.edu");
                usuarioAluno.setTelefone("62988888888");
                usuarioAluno.setTipoUsuario(tipoAluno);
                usuarioAluno.setSenha(passwordEncoder.encode("12345"));
                usuarioAluno = usuarioRepository.save(usuarioAluno);
                Responsavel responsavel = new Responsavel();
                responsavel.setNome("Mãe do Aluno Padrão");
                responsavel.setCpf("99988877766");
                responsavel.setTelefone("62911111111");
                responsavel.setEmail("mae.padrao@email.com");
                responsavel = responsavelRepository.save(responsavel);
                Aluno alunoEntity = new Aluno();
                alunoEntity.setUsuario(usuarioAluno);       // Vincula ao usuário
                alunoEntity.setResponsavel(responsavel);    // Vincula ao responsável
                alunoRepository.save(alunoEntity);          // Salva o vínculo
                System.out.println("Usuário 'aluno' e seu Responsável inseridos com sucesso.");
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
                professor.setSenha(passwordEncoder.encode("12345"));
                usuarioRepository.save(professor);
                System.out.println("Usuário 'professor' inserido com sucesso.");
            } else {
                System.out.println("Usuário 'professor' já existe.");
            }
        };
    }
}