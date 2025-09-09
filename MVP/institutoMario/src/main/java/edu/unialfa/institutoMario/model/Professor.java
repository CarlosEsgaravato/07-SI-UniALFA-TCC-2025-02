package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    @JsonManagedReference
    private Usuario usuario;

    @OneToMany(mappedBy = "professor")
    @JsonBackReference
    private List<Disciplina> disciplinas = new ArrayList<>();
}

