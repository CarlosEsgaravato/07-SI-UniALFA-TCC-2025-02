package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.dto.EventoDto;
import edu.unialfa.institutoMario.model.Evento; // Garanta que o nome da entidade está correto
import edu.unialfa.institutoMario.service.EventoService;
import edu.unialfa.institutoMario.service.TurmaService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final TurmaService turmaService;

    @GetMapping
    public String exibirCalendario() {
        return "eventos/calendario"; // Retorna a página "vazia"
    }

    @GetMapping("/dados-calendario")
    @ResponseBody
    public List<EventoDto> getEventosParaCalendario() {
        List<Evento> todosOsEventos = eventoService.listarTodos();
        return todosOsEventos.stream()
                .map(EventoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/novo")
    public String novoEventoForm(Model model, @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        Evento evento = new Evento();
        evento.setData(data);
        model.addAttribute("evento", evento);
        model.addAttribute("turmas", turmaService.listarTodas());
        return "eventos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarEventoForm(Model model, @PathVariable Long id) {
        Evento evento = eventoService.buscarPorId(id);
        model.addAttribute("evento", evento);
        model.addAttribute("turmas", turmaService.listarTodas());
        return "eventos/formulario";
    }


    @PostMapping("/salvar")
    public String salvarEvento(@ModelAttribute("evento") Evento evento) {
        eventoService.salvar(evento);
        return "redirect:/eventos";
    }

    @GetMapping("/deletar/{id}")
    public String deletarEvento(@PathVariable Long id) {
        eventoService.deletarPorId(id);
        return "redirect:/eventos";
    }
}