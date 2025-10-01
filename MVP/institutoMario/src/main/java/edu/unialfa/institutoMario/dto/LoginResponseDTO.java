package edu.unialfa.institutoMario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String nome;
    private String email;
    private String tipo;
}