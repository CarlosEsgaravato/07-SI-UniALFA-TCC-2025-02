package edu.unialfa.institutoMario.api;

import edu.unialfa.institutoMario.dto.LoginRequestDTO;
import edu.unialfa.institutoMario.dto.LoginResponseDTO;
import edu.unialfa.institutoMario.model.Usuario;
import edu.unialfa.institutoMario.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getId(), data.getPassword());

        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.gerarToken(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        LoginResponseDTO response = new LoginResponseDTO(
                token,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoUsuario().getDescricao()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        String tokenValue = token.replace("Bearer ", "");
        boolean isValid = tokenService.isTokenValido(tokenValue);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}