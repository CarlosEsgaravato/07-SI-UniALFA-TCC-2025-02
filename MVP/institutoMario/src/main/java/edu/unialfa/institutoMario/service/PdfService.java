package edu.unialfa.institutoMario.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.model.Questao;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public ByteArrayInputStream gerarPdfDaProva(Prova prova) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);

        document.open();

        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fonteCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font fonteNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font fonteQuestao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font fonteAlternativa = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph titulo = new Paragraph(prova.getTitulo(), fonteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(Chunk.NEWLINE);

        PdfPTable cabecalhoTable = new PdfPTable(2);
        cabecalhoTable.setWidthPercentage(100);

        String nomeTurma = "____________________";
        if (prova.getDisciplina() != null && prova.getDisciplina().getTurma() != null) {
            nomeTurma = prova.getDisciplina().getTurma().getNome();
        }

        cabecalhoTable.addCell(criarCelula("Aluno(a): ____________________________________________", fonteNormal));
        cabecalhoTable.addCell(criarCelula("Turma: " + nomeTurma, fonteNormal));
        cabecalhoTable.addCell(criarCelula("Professor(a): " + prova.getDisciplina().getProfessor().getUsuario().getNome(), fonteNormal));
        cabecalhoTable.addCell(criarCelula("Matéria: " + prova.getDisciplina().getNome(), fonteNormal));
        cabecalhoTable.addCell(criarCelula("Data: " + prova.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fonteNormal));
        cabecalhoTable.addCell(criarCelula("Nota: ________", fonteNormal));
        document.add(cabecalhoTable);
        document.add(Chunk.NEWLINE);

        // --- GABARITO ---
        Paragraph tituloGabarito = new Paragraph("GABARITO DE RESPOSTAS", fonteCabecalho);
        tituloGabarito.setAlignment(Element.ALIGN_CENTER);
        document.add(tituloGabarito);
        document.add(Chunk.NEWLINE);
        PdfPTable gabaritoTable = new PdfPTable(6);
        gabaritoTable.setWidthPercentage(100);
        gabaritoTable.addCell(criarCelulaCentralizada("Questão", fonteCabecalho));
        gabaritoTable.addCell(criarCelulaCentralizada("A", fonteCabecalho));
        gabaritoTable.addCell(criarCelulaCentralizada("B", fonteCabecalho));
        gabaritoTable.addCell(criarCelulaCentralizada("C", fonteCabecalho));
        gabaritoTable.addCell(criarCelulaCentralizada("D", fonteCabecalho));
        gabaritoTable.addCell(criarCelulaCentralizada("E", fonteCabecalho));
        for (Questao questao : prova.getQuestoes()) {
            gabaritoTable.addCell(criarCelulaCentralizada(questao.getNumero(), fonteQuestao));
            for (int i = 0; i < 5; i++) {
                gabaritoTable.addCell(criarCelulaCentralizada("(   )", fonteNormal));
            }
        }
        document.add(gabaritoTable);
        document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
        document.add(Chunk.NEWLINE);

        // --- QUESTÕES ---
        for (Questao questao : prova.getQuestoes()) {
            PdfPTable questaoWrapper = new PdfPTable(1);
            questaoWrapper.setWidthPercentage(100);
            questaoWrapper.setKeepTogether(true);
            PdfPCell cell = criarCelula("", fonteNormal);
            cell.addElement(new Paragraph(questao.getNumero() + ". " + questao.getEnunciado(), fonteQuestao));
            cell.addElement(new Paragraph(" "));
            cell.addElement(new Paragraph("A) " + questao.getAlternativaA(), fonteAlternativa));
            cell.addElement(new Paragraph("B) " + questao.getAlternativaB(), fonteAlternativa));
            cell.addElement(new Paragraph("C) " + questao.getAlternativaC(), fonteAlternativa));
            cell.addElement(new Paragraph("D) " + questao.getAlternativaD(), fonteAlternativa));
            cell.addElement(new Paragraph("E) " + questao.getAlternativaE(), fonteAlternativa));
            questaoWrapper.addCell(cell);
            document.add(questaoWrapper);
            document.add(Chunk.NEWLINE);
        }

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private PdfPCell criarCelula(String texto, Font fonte) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fonte));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell criarCelulaCentralizada(String texto, Font fonte) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fonte));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }
}