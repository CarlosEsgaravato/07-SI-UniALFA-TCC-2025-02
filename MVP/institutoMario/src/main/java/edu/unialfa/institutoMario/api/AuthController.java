package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.dto.LoginRequestDTO;
import edu.unialfa.institutoMario.dto.LoginResponseDTO;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.getId(), data.getPassword());


            var auth = this.authenticationManager.authenticate(usernamePassword);


            Usuario usuario = (Usuario) auth.getPrincipal();

            Long tipoId = usuario.getTipoUsuario().getId();

            if (tipoId!= 2L & tipoId!= 1l) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Este aplicativo é de uso exclusivo para Administradores e professores."));
            }
            var token = tokenService.gerarToken(auth);

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getTipoUsuario().getDescricao()
            );
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "ID de usuário ou senha inválidos."));
        }
    }


    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        String tokenValue = token.replace("Bearer ", "");
        boolean isValid = tokenService.isTokenValido(tokenValue);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}