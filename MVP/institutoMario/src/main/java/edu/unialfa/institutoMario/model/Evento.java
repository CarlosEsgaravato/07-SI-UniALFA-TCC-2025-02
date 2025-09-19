package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeEvento;

    @ManyToOne
    @JoinColumn(name = "id_turma")
    @JsonManagedReference
    private Turma turma;

    private LocalDateTime data;

    private String local;

    private String observacao;

    private String imagemUrl;
}
