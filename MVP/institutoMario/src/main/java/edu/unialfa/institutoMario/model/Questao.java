package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    private BigDecimal pontuacao;

    private String alternativaCorreta;

    @ManyToOne
    @JoinColumn(name = "id_prova")
    @JsonManagedReference
    private Prova prova;
}
