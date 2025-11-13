package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // NOVO
import jakarta.validation.constraints.NotNull; // NOVO
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Projetos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "O nome do projeto é obrigatório.") // NOVO: Validação
    private String nomeProjeto;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("projeto-documento")
    private List<Documento> documentos;

    @ManyToOne(optional = true) // Garante que o JPA/Hibernate entende que é opcional
    @JoinColumn(name = "id_turma", nullable = true) // Garante que a coluna aceita NULL
    @JsonManagedReference("projeto-turma")
    private Turma turma;
}