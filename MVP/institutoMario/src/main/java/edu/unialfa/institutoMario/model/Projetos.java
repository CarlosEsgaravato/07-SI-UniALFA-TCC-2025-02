package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Data
public class Projetos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String nomeProjeto;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("projeto-documento")
    private List<Documento> documentos;

    @ManyToOne
    @JoinColumn(name = "id_turma")
    @JsonManagedReference("projeto-turma")
    private Turma turma;
}