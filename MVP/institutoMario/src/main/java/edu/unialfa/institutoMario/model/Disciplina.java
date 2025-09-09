package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "id_turma")
    @JsonManagedReference
    private Turma turma;

    @ManyToOne
    @JoinColumn(name = "id_professor")
    @JsonManagedReference
    private Professor professor;

    @OneToMany(mappedBy = "disciplina")
    @JsonBackReference
    private List<Prova> provas = new ArrayList<>();
}
