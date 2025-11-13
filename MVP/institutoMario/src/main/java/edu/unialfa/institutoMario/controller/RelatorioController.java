package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.format.annotation.DateTimeFormat;
import edu.unialfa.institutoMario.model.Evento;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.service.ProvaService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/relatorios")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
public class RelatorioController {

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private DisciplinaService disciplinaService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private ProvaService provaService;

    @GetMapping
    public String centralDeRelatorios() {
        return "relatorios/relatorios";
    }

    // RELATÓRIO: ALUNOS POR TURMA

    @GetMapping("/alunos-por-turma")
    public String getPaginaAlunosPorTurma(Model model) {
        model.addAttribute("turmas", turmaService.listarTodas());
        model.addAttribute("alunos", Collections.emptyList());
        model.addAttribute("turmaSelecionadaId", 0L);
        return "relatorios/alunos-por-turma";
    }

    @PostMapping("/alunos-por-turma")
    public String postGerarRelatorioAlunosPorTurma(
            @RequestParam(required = false) Long turmaId,
            Model model
    ) {
        List<Aluno> alunos = alunoService.listarAlunosPorTurmaId(turmaId);

        model.addAttribute("turmas", turmaService.listarTodas());
        model.addAttribute("alunos", alunos);
        model.addAttribute("turmaSelecionadaId", turmaId);

        return "relatorios/alunos-por-turma";
    }

    @GetMapping("/alunos-por-turma/export/excel")
    public void exportarAlunosExcel(@RequestParam Long turmaId, HttpServletResponse response) throws IOException {
        List<Aluno> alunos = alunoService.listarAlunosPorTurmaId(turmaId);
        reportService.gerarExcelAlunosPorTurma(alunos, response);
    }

    @GetMapping("/alunos-por-turma/export/pdf")
    public void exportarAlunosPdf(@RequestParam Long turmaId, HttpServletResponse response) throws IOException {
        List<Aluno> alunos = alunoService.listarAlunosPorTurmaId(turmaId);
        reportService.gerarPdfAlunosPorTurma(alunos, response);
    }

    // RELATÓRIO: DISCIPLINAS POR TURMA

    @GetMapping("/disciplinas-por-turma")
    public String getPaginaDisciplinasPorTurma(Model model) {
        model.addAttribute("turmas", turmaService.listarTodas());
        model.addAttribute("disciplinas", Collections.emptyList());
        model.addAttribute("turmaSelecionadaId", 0L);
        return "relatorios/disciplinas-por-turma";
    }

    @PostMapping("/disciplinas-por-turma")
    public String postGerarRelatorioDisciplinasPorTurma(
            @RequestParam(required = false) Long turmaId,
            Model model
    ) {
        List<Disciplina> disciplinas = disciplinaService.listarDisciplinasPorTurmaId(turmaId);

        model.addAttribute("turmas", turmaService.listarTodas());
        model.addAttribute("disciplinas", disciplinas);
        model.addAttribute("turmaSelecionadaId", turmaId);

        return "relatorios/disciplinas-por-turma";
    }

    @GetMapping("/disciplinas-por-turma/export/excel")
    public void exportarDisciplinasExcel(@RequestParam Long turmaId, HttpServletResponse response) throws IOException {
        List<Disciplina> disciplinas = disciplinaService.listarDisciplinasPorTurmaId(turmaId);
        reportService.gerarExcelDisciplinasPorTurma(disciplinas, response);
    }

    @GetMapping("/disciplinas-por-turma/export/pdf")
    public void exportarDisciplinasPdf(@RequestParam Long turmaId, HttpServletResponse response) throws IOException {
        List<Disciplina> disciplinas = disciplinaService.listarDisciplinasPorTurmaId(turmaId);
        reportService.gerarPdfDisciplinasPorTurma(disciplinas, response);
    }

    // RELATÓRIO: EVENTOS POR PERÍODO

    @GetMapping("/eventos-por-periodo")
    public String getPaginaEventosPorPeriodo(Model model) {
        model.addAttribute("eventos", Collections.emptyList());
        model.addAttribute("dataInicio", null);
        model.addAttribute("dataFim", null);
        return "relatorios/eventos-por-periodo";
    }

    @PostMapping("/eventos-por-periodo")
    public String postGerarRelatorioEventosPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Model model
    ) {
        // Validação do período no backend
        if (dataInicio != null && dataFim != null) {
            long diferencaDias = ChronoUnit.DAYS.between(dataInicio, dataFim);

            if (diferencaDias > 60) {
                model.addAttribute("eventos", Collections.emptyList());
                model.addAttribute("erro", "O período não pode ultrapassar 60 dias!");
                model.addAttribute("dataInicio", dataInicio);
                model.addAttribute("dataFim", dataFim);
                return "relatorios/eventos-por-periodo";
            }

            if (diferencaDias < 0) {
                model.addAttribute("eventos", Collections.emptyList());
                model.addAttribute("erro", "A data fim deve ser maior que a data início!");
                model.addAttribute("dataInicio", dataInicio);
                model.addAttribute("dataFim", dataFim);
                return "relatorios/eventos-por-periodo";
            }
        }

        List<Evento> eventos = eventoService.listarEventosPorPeriodo(dataInicio, dataFim);

        model.addAttribute("eventos", eventos);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);

        return "relatorios/eventos-por-periodo";
    }

    @GetMapping("/eventos-por-periodo/export/excel")
    public void exportarEventosExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletResponse response
    ) throws IOException {
        List<Evento> eventos = eventoService.listarEventosPorPeriodo(dataInicio, dataFim);
        reportService.gerarExcelEventosPorPeriodo(eventos, response);
    }

    @GetMapping("/eventos-por-periodo/export/pdf")
    public void exportarEventosPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletResponse response
    ) throws IOException {
        List<Evento> eventos = eventoService.listarEventosPorPeriodo(dataInicio, dataFim);
        reportService.gerarPdfEventosPorPeriodo(eventos, response);
    }

    // RELATÓRIO: PROVAS POR DISCIPLINA

    @GetMapping("/provas-por-disciplina")
    public String getPaginaProvasPorDisciplina(Model model) {
        model.addAttribute("disciplinas", disciplinaService.listarTodas());
        model.addAttribute("provas", Collections.emptyList());
        model.addAttribute("disciplinaSelecionadaId", 0L);
        return "relatorios/provas-por-disciplina";
    }

    @PostMapping("/provas-por-disciplina")
    public String postGerarRelatorioProvasPorDisciplina(
            @RequestParam(required = false) Long disciplinaId,
            Model model
    ) {
        List<Prova> provas = provaService.listarPorDisciplina(disciplinaId);

        model.addAttribute("disciplinas", disciplinaService.listarTodas());
        model.addAttribute("provas", provas);
        model.addAttribute("disciplinaSelecionadaId", disciplinaId);

        return "relatorios/provas-por-disciplina";
    }

    @GetMapping("/provas-por-disciplina/export/excel")
    public void exportarProvasExcel(@RequestParam Long disciplinaId, HttpServletResponse response) throws IOException {
        List<Prova> provas = provaService.listarPorDisciplina(disciplinaId);
        reportService.gerarExcelProvasPorDisciplina(provas, response);
    }

    @GetMapping("/provas-por-disciplina/export/pdf")
    public void exportarProvasPdf(@RequestParam Long disciplinaId, HttpServletResponse response) throws IOException {
        List<Prova> provas = provaService.listarPorDisciplina(disciplinaId);
        reportService.gerarPdfProvasPorDisciplina(provas, response);
    }
}