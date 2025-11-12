package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Disciplina;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.service.AlunoService;
import edu.unialfa.institutoMario.service.DisciplinaService;
import edu.unialfa.institutoMario.service.ReportService;
import edu.unialfa.institutoMario.service.TurmaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}