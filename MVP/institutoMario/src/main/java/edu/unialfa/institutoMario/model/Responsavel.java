package edu.unialfa.institutoMario.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Responsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String nome;
    private String Cpf;
    private String telefone;
    private String email;
}
