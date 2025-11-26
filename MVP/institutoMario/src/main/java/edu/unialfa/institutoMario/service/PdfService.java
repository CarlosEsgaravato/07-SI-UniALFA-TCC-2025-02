package edu.unialfa.institutoMario.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.model.Questao;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;

@Service
public class PdfService {

    private static final Font F_INSTITUICAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font F_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font F_VALOR = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

    private static final Font F_QUESTAO_NUM = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font F_QUESTAO_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font F_ALTERNATIVA = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

    private static final Font F_GAB_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
    private static final Font F_GAB_NUMERO = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
    private static final Font F_GAB_LETRA = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

    public ByteArrayInputStream gerarPdfDaProva(Prova prova) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 30, 30);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            document.add(criarCabecalhoCompleto(prova));

            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 8)));

            document.add(criarGabaritoCompacto(prova.getQuestoes()));

            LineSeparator separator = new LineSeparator(1f, 100f, Color.BLACK, Element.ALIGN_CENTER, -2);
            document.add(separator);
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10)));

            for (Questao questao : prova.getQuestoes()) {
                document.add(criarBlocoQuestao(questao));
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private PdfPTable criarCabecalhoCompleto(Prova prova) throws DocumentException {
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        PdfPCell wrapper = new PdfPCell();
        wrapper.setBorder(Rectangle.BOX);
        wrapper.setBorderWidth(1.5f);
        wrapper.setBorderColor(Color.BLACK);
        wrapper.setPadding(5);

        PdfPTable topHeader = new PdfPTable(2);
        topHeader.setWidthPercentage(100);
        topHeader.setWidths(new float[]{2f, 8f});

        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellLogo.setHorizontalAlignment(Element.ALIGN_CENTER);

        try {
            URL imageUrl = getClass().getResource("/static/images/logo2.png");

            if (imageUrl == null) imageUrl = getClass().getResource("/images/logo2.png");

            if (imageUrl != null) {
                Image logo = Image.getInstance(imageUrl);
                logo.scaleToFit(70, 70);
                cellLogo.addElement(logo);
            } else {
                cellLogo.addElement(new Phrase("LOGO", F_LABEL));
            }
        } catch (Exception e) {
            cellLogo.addElement(new Phrase(" ", F_LABEL));
        }
        topHeader.addCell(cellLogo);
        PdfPCell cellTexto = new PdfPCell();
        cellTexto.setBorder(Rectangle.NO_BORDER);
        cellTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTexto.setPaddingLeft(10);

        Paragraph pInst = new Paragraph("INSTITUTO MÁRIO", F_INSTITUICAO);
        Paragraph pSub = new Paragraph("AVALIAÇÃO", F_TITULO);

        cellTexto.addElement(pInst);
        cellTexto.addElement(pSub);
        topHeader.addCell(cellTexto);
        wrapper.addElement(topHeader);

        LineSeparator line = new LineSeparator(0.5f, 100, Color.BLACK, Element.ALIGN_CENTER, -2);
        wrapper.addElement(new Paragraph(" ")); // Espaço pequeno
        wrapper.addElement(line);
        wrapper.addElement(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
        String nomeTurma = (prova.getDisciplina() != null && prova.getDisciplina().getTurma() != null)
                ? prova.getDisciplina().getTurma().getNome() : "";
        String nomeProfessor = (prova.getDisciplina().getProfessor() != null)
                ? prova.getDisciplina().getProfessor().getUsuario().getNome() : "";
        String nomeDisciplina = prova.getDisciplina().getNome();

        PdfPTable grid = new PdfPTable(4);
        grid.setWidthPercentage(100);
        grid.setWidths(new float[]{1.2f, 3.8f, 1.5f, 3.5f});

        grid.addCell(criarCelulaLabel("Aluno(a):"));
        grid.addCell(criarCelulaValor(""));
        grid.addCell(criarCelulaLabel("Turma:"));
        grid.addCell(criarCelulaValor(nomeTurma));

        grid.addCell(criarCelulaLabel("Disciplina:"));
        grid.addCell(criarCelulaValor(nomeDisciplina));
        grid.addCell(criarCelulaLabel("Professor:"));
        grid.addCell(criarCelulaValor(nomeProfessor));

        grid.addCell(criarCelulaLabel("Data:"));
        grid.addCell(criarCelulaValor("___/___/_____"));
        grid.addCell(criarCelulaLabel("Nota:"));
        grid.addCell(criarCelulaValor(""));

        wrapper.addElement(grid);
        mainTable.addCell(wrapper);

        return mainTable;
    }

    private PdfPCell criarCelulaLabel(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_LABEL));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(4);
        return cell;
    }

    private PdfPCell criarCelulaValor(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_VALOR));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingBottom(2);
        cell.setMinimumHeight(16f);
        return cell;
    }

    private PdfPTable criarGabaritoCompacto(List<Questao> questoes) throws DocumentException {
        PdfPTable mainWrapper = new PdfPTable(1);
        mainWrapper.setWidthPercentage(100);
        mainWrapper.setSpacingBefore(5);
        mainWrapper.setSpacingAfter(10);

        // Título "GABARITO"
        Paragraph titulo = new Paragraph("GABARITO", F_GAB_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);

        PdfPCell tituloCell = new PdfPCell(titulo);
        tituloCell.setBorder(Rectangle.NO_BORDER);
        tituloCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainWrapper.addCell(tituloCell);

        PdfPTable gabaritoTable = new PdfPTable(6);

        gabaritoTable.setTotalWidth(120f);
        gabaritoTable.setLockedWidth(true);

        gabaritoTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        gabaritoTable.setWidths(new float[]{1.4f, 1f, 1f, 1f, 1f, 1f});

        float alturaCelula = 14f;

        String[] headers = {"Q", "A", "B", "C", "D", "E"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, F_GAB_LETRA));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setBackgroundColor(Color.LIGHT_GRAY);
            headerCell.setBorder(Rectangle.BOX);
            headerCell.setBorderWidth(0.5f);
            headerCell.setFixedHeight(alturaCelula);
            gabaritoTable.addCell(headerCell);
        }

        for (Questao questao : questoes) {
            PdfPCell numCell = new PdfPCell(new Phrase(questao.getNumero(), F_GAB_NUMERO));
            numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            numCell.setBorder(Rectangle.BOX);
            numCell.setBorderWidth(0.5f);
            numCell.setFixedHeight(alturaCelula);
            gabaritoTable.addCell(numCell);

            for (int i = 0; i < 5; i++) {
                PdfPCell circleCell = new PdfPCell();
                circleCell.setBorder(Rectangle.BOX);
                circleCell.setBorderWidth(0.5f);
                circleCell.setFixedHeight(alturaCelula);

                circleCell.setCellEvent(new CircleEvent(3.0f));
                gabaritoTable.addCell(circleCell);
            }
        }

        PdfPCell innerCell = new PdfPCell(gabaritoTable);
        innerCell.setBorder(Rectangle.NO_BORDER);
        innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainWrapper.addCell(innerCell);

        return mainWrapper;
    }

    private static class CircleEvent implements PdfPCellEvent {
        private final float radius;

        public CircleEvent(float radius) {
            this.radius = radius;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            if (canvas != null) {
                canvas.setColorStroke(Color.BLACK);
                canvas.setLineWidth(0.6f);

                float centerX = position.getLeft() + (position.getWidth() / 2);
                float centerY = position.getTop() - (position.getHeight() / 2);

                canvas.circle(centerX, centerY, radius);
                canvas.stroke();
            }
        }
    }

    private PdfPTable criarBlocoQuestao(Questao questao) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setKeepTogether(true);
        wrapper.setSpacingAfter(10f);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0f);

        Paragraph pNumero = new Paragraph();
        pNumero.add(new Chunk(questao.getNumero() + ") ", F_QUESTAO_NUM));
        pNumero.add(new Chunk(questao.getEnunciado(), F_QUESTAO_TEXTO));
        pNumero.setSpacingAfter(8f);
        pNumero.setLeading(13f);
        cell.addElement(pNumero);

        cell.addElement(criarAlternativa("A", questao.getAlternativaA()));
        cell.addElement(criarAlternativa("B", questao.getAlternativaB()));
        cell.addElement(criarAlternativa("C", questao.getAlternativaC()));
        cell.addElement(criarAlternativa("D", questao.getAlternativaD()));
        cell.addElement(criarAlternativa("E", questao.getAlternativaE()));

        wrapper.addCell(cell);
        return wrapper;
    }

    private Paragraph criarAlternativa(String letra, String texto) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(letra + ") ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK)));
        p.add(new Chunk(texto, F_ALTERNATIVA));
        p.setIndentationLeft(20f);
        p.setSpacingAfter(4f);
        p.setLeading(12f);
        return p;
    }
}