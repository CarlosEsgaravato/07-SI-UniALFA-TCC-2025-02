package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // NOVO
import jakarta.validation.constraints.NotNull; // NOVO
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Projetos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @NotBlank(message = "O nome do projeto é obrigatório.")
    private String nomeProjeto;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("projeto-documento")
    private List<Documento> documentos;

    @ManyToMany
    @JoinTable(
            name = "projeto_turmas",
            joinColumns = @JoinColumn(name = "projeto_id"),
            inverseJoinColumns = @JoinColumn(name = "turma_id")
    )
    @ToString.Exclude
    private List<Turma> turmas = new ArrayList<>();
}