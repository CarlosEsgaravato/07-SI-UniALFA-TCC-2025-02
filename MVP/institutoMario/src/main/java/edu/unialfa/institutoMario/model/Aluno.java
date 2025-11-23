package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    @JsonManagedReference
    private Usuario usuario;

    @ManyToMany
    @JoinTable(
            name = "aluno_turmas",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "turma_id")
    )
    @ToString.Exclude
    private List<Turma> turmas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_responsavel")
    private Responsavel responsavel;
}
