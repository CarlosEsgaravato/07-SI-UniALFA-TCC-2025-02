package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Aluno;
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


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    private void setResponseHeaders(HttpServletResponse response, String contentType, String extension) {

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=relatorio_alunos_" + currentDateTime + extension;

        response.setContentType(contentType);
        response.setHeader(headerKey, headerValue);
    }

    public void gerarExcelAlunosPorTurma(List<Aluno> alunos, HttpServletResponse response) throws IOException {
        setResponseHeaders(response, "application/octet-stream", ".xlsx");

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
        setResponseHeaders(response, "application/pdf", ".pdf");

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Relatório de Alunos por Turma").setBold().setFontSize(18));

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
}