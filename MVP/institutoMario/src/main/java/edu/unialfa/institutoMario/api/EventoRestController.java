package edu.unialfa.institutoMario.api;


import edu.unialfa.institutoMario.dto.EventoDto;
import edu.unialfa.institutoMario.model.Evento;
import edu.unialfa.institutoMario.service.EventoService;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/evento")
public class EventoRestController {

    private final EventoService eventoService;
    private final TurmaService turmaService;

    @GetMapping
    public ResponseEntity<List<EventoDto>> listarTodosEventos(){
        List<Evento> eventos = eventoService.listarTodos();
        List<EventoDto> dtos = eventos.stream().map(EventoDto::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDto> buscarEventoPorId(@PathVariable Long id) {
        try {
            Evento evento = eventoService.buscarPorId(id);
            EventoDto dto = EventoDto.fromEntity(evento);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
