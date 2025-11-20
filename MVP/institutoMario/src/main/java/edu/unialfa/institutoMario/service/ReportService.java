package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Disciplina;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import edu.unialfa.institutoMario.model.Evento;
import java.time.format.DateTimeFormatter;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.dto.NotaPorDisciplinaDTO;
import edu.unialfa.institutoMario.dto.NotaDTO;
import java.math.BigDecimal;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import java.math.RoundingMode;
import org.springframework.core.io.ClassPathResource;
import com.itextpdf.io.image.ImageData;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    /**
     * Adiciona o cabeçalho com logo e título ao documento PDF
     */
    private void adicionarCabecalhoPDF(Document document, String titulo) {
        try {
            // Carrega a logo do diretório static/images
            ClassPathResource imgFile = new ClassPathResource("static/images/logo2.png");
            InputStream inputStream = imgFile.getInputStream();
            byte[] imageBytes = inputStream.readAllBytes();

            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image logo = new Image(imageData);

            // Ajusta o tamanho da logo
            logo.scaleToFit(100, 50);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
            logo.setMarginBottom(10);

            // Adiciona a logo
            document.add(logo);

            // Adiciona o título
            Paragraph tituloRelatorio = new Paragraph(titulo)
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(tituloRelatorio);

            // Adiciona subtítulo com nome da instituição
            Paragraph subtitulo = new Paragraph("Instituto Mário Gazin")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(20);
            document.add(subtitulo);

        } catch (Exception e) {
            // Se não conseguir carregar a logo, apenas adiciona o título
            System.err.println("Erro ao carregar logo: " + e.getMessage());
            Paragraph tituloRelatorio = new Paragraph(titulo)
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(tituloRelatorio);
        }
    }

    private void setResponseHeaders(HttpServletResponse response, String contentType, String extension, String tipoRelatorio) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=relatorio_" + tipoRelatorio + "_" + currentDateTime + extension;

        response.setContentType(contentType);
        response.setHeader(headerKey, headerValue);
    }

    // RELATÓRIO: ALUNOS POR TURMA

    public void gerarExcelAlunosPorTurma(List<Aluno> alunos, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/octet-stream", ".xlsx", "alunos");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Alunos");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Nome", "Email", "Telefone", "CPF"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Aluno aluno : alunos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(aluno.getUsuario().getNome());
                row.createCell(1).setCellValue(aluno.getUsuario().getEmail());
                row.createCell(2).setCellValue(aluno.getUsuario().getTelefone());
                row.createCell(3).setCellValue(aluno.getUsuario().getCpf());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void gerarPdfAlunosPorTurma(List<Aluno> alunos, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/pdf", ".pdf", "alunos");

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Adiciona cabeçalho com logo
            adicionarCabecalhoPDF(document, "Relatório de Alunos por Turma");

            float[] columnWidths = {4, 4, 3, 3};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(20);

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Nome").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Email").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Telefone").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("CPF").setBold()));

            for (Aluno aluno : alunos) {
                table.addCell(aluno.getUsuario().getNome());
                table.addCell(aluno.getUsuario().getEmail());
                table.addCell(aluno.getUsuario().getTelefone());
                table.addCell(aluno.getUsuario().getCpf());
            }

            document.add(table);
        }
    }

    // RELATÓRIO: DISCIPLINAS POR TURMA

    public void gerarExcelDisciplinasPorTurma(List<Disciplina> disciplinas, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/octet-stream", ".xlsx", "disciplinas");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Disciplinas");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Nome", "Turma", "Professor"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Disciplina disciplina : disciplinas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(disciplina.getNome());
                row.createCell(1).setCellValue(disciplina.getTurma() != null ?
                        disciplina.getTurma().getNome() : "-");
                row.createCell(2).setCellValue(disciplina.getProfessor() != null ?
                        disciplina.getProfessor().getUsuario().getNome() : "Não atribuído");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void gerarPdfDisciplinasPorTurma(List<Disciplina> disciplinas, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/pdf", ".pdf", "disciplinas");

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Adiciona cabeçalho com logo
            adicionarCabecalhoPDF(document, "Relatório de Disciplinas por Turma");

            float[] columnWidths = {4, 3, 3};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(20);

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Nome").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Turma").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Professor").setBold()));

            for (Disciplina disciplina : disciplinas) {
                table.addCell(disciplina.getNome());
                table.addCell(disciplina.getTurma() != null ?
                        disciplina.getTurma().getNome() : "-");
                table.addCell(disciplina.getProfessor() != null ?
                        disciplina.getProfessor().getUsuario().getNome() : "Não atribuído");
            }

            document.add(table);
        }
    }

    // RELATÓRIO: EVENTOS POR PERÍODO

    public void gerarExcelEventosPorPeriodo(List<Evento> eventos, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/octet-stream", ".xlsx", "eventos");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Eventos");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Nome do Evento", "Turma", "Data", "Local"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            int rowNum = 1;
            for (Evento evento : eventos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(evento.getNomeEvento());
                row.createCell(1).setCellValue(evento.getTurma() != null ?
                        evento.getTurma().getNome() : "-");
                row.createCell(2).setCellValue(evento.getData() != null ?
                        evento.getData().format(formatter) : "-");
                row.createCell(3).setCellValue(evento.getLocal() != null ?
                        evento.getLocal() : "-");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void gerarPdfEventosPorPeriodo(List<Evento> eventos, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/pdf", ".pdf", "eventos");

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Adiciona cabeçalho com logo
            adicionarCabecalhoPDF(document, "Relatório de Eventos por Período");

            float[] columnWidths = {3, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(20);

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Nome do Evento").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Turma").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Data").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Local").setBold()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Evento evento : eventos) {
                table.addCell(evento.getNomeEvento());
                table.addCell(evento.getTurma() != null ?
                        evento.getTurma().getNome() : "-");
                table.addCell(evento.getData() != null ?
                        evento.getData().format(formatter) : "-");
                table.addCell(evento.getLocal() != null ?
                        evento.getLocal() : "-");
            }

            document.add(table);
        }
    }

    // RELATÓRIO: PROVAS POR DISCIPLINA

    public void gerarExcelProvasPorDisciplina(List<Prova> provas, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/octet-stream", ".xlsx", "provas");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Provas");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Título", "Data", "Disciplina", "Professor"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowNum = 1;
            for (Prova prova : provas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(prova.getTitulo());
                row.createCell(1).setCellValue(prova.getData() != null ?
                        prova.getData().format(formatter) : "-");
                row.createCell(2).setCellValue(prova.getDisciplina() != null ?
                        prova.getDisciplina().getNome() : "-");
                row.createCell(3).setCellValue(prova.getDisciplina() != null && prova.getDisciplina().getProfessor() != null ?
                        prova.getDisciplina().getProfessor().getUsuario().getNome() : "Não atribuído");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void gerarPdfProvasPorDisciplina(List<Prova> provas, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/pdf", ".pdf", "provas");

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Adiciona cabeçalho com logo
            adicionarCabecalhoPDF(document, "Relatório de Provas por Disciplina");

            float[] columnWidths = {3, 2, 3, 3};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(20);

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Título").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Data").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Disciplina").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Professor").setBold()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Prova prova : provas) {
                table.addCell(prova.getTitulo());
                table.addCell(prova.getData() != null ?
                        prova.getData().format(formatter) : "-");
                table.addCell(prova.getDisciplina() != null ?
                        prova.getDisciplina().getNome() : "-");
                table.addCell(prova.getDisciplina() != null && prova.getDisciplina().getProfessor() != null ?
                        prova.getDisciplina().getProfessor().getUsuario().getNome() : "Não atribuído");
            }

            document.add(table);
        }
    }

    // RELATÓRIO: NOTAS POR ALUNO

    public void gerarExcelNotasPorAluno(List<NotaPorDisciplinaDTO> notasAgrupadas, String alunoNome, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/octet-stream", ".xlsx", "notas_" + alunoNome.replace(" ", "_"));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Notas");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Relatório de Notas - " + alunoNome);
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

            Row headerRow = sheet.createRow(2);
            String[] headers = {"Disciplina", "Data", "Nota", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowNum = 3;
            for (NotaPorDisciplinaDTO agrupamento : notasAgrupadas) {
                for (NotaDTO nota : agrupamento.getNotas()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(nota.getDisciplinaNome());
                    row.createCell(1).setCellValue(nota.getData() != null ?
                            nota.getData().format(formatter) : "-");
                    row.createCell(2).setCellValue(nota.getNotaTotal().doubleValue());

                    String status = nota.getNotaTotal().compareTo(new BigDecimal("7.0")) >= 0 ?
                            "Acima da média" : "Abaixo da média";
                    row.createCell(3).setCellValue(status);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void gerarPdfNotasPorAluno(List<NotaPorDisciplinaDTO> notasAgrupadas, String alunoNome, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/pdf", ".pdf", "notas_" + alunoNome.replace(" ", "_"));

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Adiciona cabeçalho com logo
            adicionarCabecalhoPDF(document, "Relatório de Notas por Aluno");

            // Adiciona nome do aluno
            document.add(new Paragraph("Aluno: " + alunoNome)
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(20));

            float[] columnWidths = {3, 2, 1.5f, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(20);

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Disciplina").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Data").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Nota").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Status").setBold()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (NotaPorDisciplinaDTO agrupamento : notasAgrupadas) {
                for (NotaDTO nota : agrupamento.getNotas()) {
                    table.addCell(nota.getDisciplinaNome());
                    table.addCell(nota.getData() != null ?
                            nota.getData().format(formatter) : "-");
                    table.addCell(nota.getNotaTotal().toString());

                    String status = nota.getNotaTotal().compareTo(new BigDecimal("7.0")) >= 0 ?
                            "Acima da média" : "Abaixo da média";
                    table.addCell(status);
                }
            }

            document.add(table);
        }
    }
}