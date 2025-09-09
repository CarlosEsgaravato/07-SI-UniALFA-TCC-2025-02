package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

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

    @ManyToOne
    @JoinColumn(name = "id_turma")
    @JsonManagedReference
    private Turma turma;
}
