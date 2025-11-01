package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.dto.EventoDto;
import edu.unialfa.institutoMario.model.Evento;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.EventoService;
import edu.unialfa.institutoMario.service.TurmaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final TurmaService turmaService;
    private final String uploadDir;

    public EventoController(EventoService eventoService,
                            TurmaService turmaService,
                            @Value("${upload.dir}") String uploadDir) {
        this.eventoService = eventoService;
        this.turmaService = turmaService;
        this.uploadDir = uploadDir;
    }

    @GetMapping
    public String exibirCalendario() {
        return "eventos/calendario";
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
    public String novoEventoForm(Model model, @RequestParam(value = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        Evento evento = new Evento();
        if (data != null) {
            evento.setData(data.atStartOfDay());
        }
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
    public String salvarEvento(@ModelAttribute("evento") Evento evento,
                               @RequestParam("imagemFile") MultipartFile imagemFile) {
        if (evento.getTurma() != null && evento.getTurma().getId() != null) {
            Turma turmaCompleta = turmaService.buscarPorId(evento.getTurma().getId());
            evento.setTurma(turmaCompleta);
        } else {
            evento.setTurma(null);
        }
        if (imagemFile != null && !imagemFile.isEmpty()) {
            try {
                String nomeArquivo = UUID.randomUUID().toString() + "_" + imagemFile.getOriginalFilename();
                Path caminhoArquivo = Paths.get(uploadDir, nomeArquivo);
                Files.copy(imagemFile.getInputStream(), caminhoArquivo);
                evento.setImagemUrl(nomeArquivo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        eventoService.salvar(evento);
        return "redirect:/eventos";
    }

    @GetMapping("/deletar/{id}")
    public String deletarEvento(@PathVariable Long id) {
        eventoService.deletarPorId(id);
        return "redirect:/eventos";
    }

    @GetMapping("/verificarHorario")
    @ResponseBody
    public boolean verificarHorarioExistente(@RequestParam("dataHora")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHora,
                                             @RequestParam(value = "idEvento", required = false) Long idEvento) {

        return eventoService.existeEventoNoHorario(dataHora, idEvento);
    }
}