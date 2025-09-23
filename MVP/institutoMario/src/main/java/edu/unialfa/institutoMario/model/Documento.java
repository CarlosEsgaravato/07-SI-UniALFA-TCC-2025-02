package edu.unialfa.institutoMario.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeArquivo;
    private String tipoArquivo;
    private String urlArquivo;

    @ManyToOne
    @JoinColumn(name = "id_projeto", nullable = false)
    @JsonBackReference("projeto-documento")
    private Projetos projeto;
}