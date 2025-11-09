package edu.unialfa.institutoMario.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.draw.LineSeparator;
import edu.unialfa.institutoMario.model.Prova;
import edu.unialfa.institutoMario.model.Questao;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    // --- CONSTANTES DE FONTES E CORES ---
    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font F_INSTITUICAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
    private static final Font F_CABECALHO_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font F_CABECALHO_VALOR = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font F_QUESTAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font F_ALTERNATIVA = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font F_TITULO_SECAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

    // --- FONTES COMPACTAS PARA GABARITO ---
    private static final Font F_GABARITO_NUM = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
    private static final Font F_GABARITO_LETRA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);
    private static final Font F_GABARITO_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);
    private static final Font F_GABARITO_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    // --- FIM DAS FONTES COMPACTAS ---

    private static final Color COR_FUNDO_CLARO = new Color(250, 250, 250);

    public ByteArrayInputStream gerarPdfDaProva(Prova prova) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            document.add(criarCabecalhoComLogo(prova));
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 6)));

            document.add(criarBlocoCabecalho(prova));
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10)));

            // Gera o gabarito com círculos vazios (estilo múltiplas colunas)
            document.add(criarBlocoGabaritoCirculos(prova.getQuestoes()));
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 8)));

            LineSeparator separator = new LineSeparator(1f, 100f, Color.BLACK, Element.ALIGN_CENTER, -2);
            document.add(separator);
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10)));

            for (Questao questao : prova.getQuestoes()) {
                document.add(criarBlocoQuestao(questao));
                document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 6)));
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private PdfPTable criarCabecalhoComLogo(Prova prova) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f});

        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellLogo.setPadding(5);

        try {
            URL logoUrl = getClass().getClassLoader().getResource("static/images/logo3.png");
            if (logoUrl == null) logoUrl = getClass().getClassLoader().getResource("images/logo3.png");

            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(70, 70);
                cellLogo.addElement(logo);
            } else {
                cellLogo.addElement(new Phrase(" ", F_CABECALHO_VALOR));
            }
        } catch (Exception e) {
            cellLogo.addElement(new Phrase(" ", F_CABECALHO_VALOR));
        }

        table.addCell(cellLogo);

        PdfPCell cellTexto = new PdfPCell();
        cellTexto.setBorder(Rectangle.NO_BORDER);
        cellTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTexto.setPadding(5);

        Paragraph instituicao = new Paragraph("INSTITUTO MÁRIO GAZIN", F_INSTITUICAO);
        instituicao.setAlignment(Element.ALIGN_CENTER);
        cellTexto.addElement(instituicao);

        Paragraph titulo = new Paragraph(prova.getTitulo(), F_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(5);
        cellTexto.addElement(titulo);

        table.addCell(cellTexto);
        return table;
    }

    private PdfPTable criarBlocoCabecalho(Prova prova) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3f});

        String nomeTurma = (prova.getDisciplina() != null && prova.getDisciplina().getTurma() != null)
                ? prova.getDisciplina().getTurma().getNome() : "";

        // Linha 1: Acadêmico(a)
        PdfPCell cellAcademicoLabel = new PdfPCell(new Phrase("Acadêmico(a):", F_CABECALHO_LABEL));
        cellAcademicoLabel.setBorder(Rectangle.BOX);
        cellAcademicoLabel.setBorderWidth(1.5f);
        cellAcademicoLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellAcademicoLabel.setPadding(5f);
        table.addCell(cellAcademicoLabel);

        PdfPCell cellAcademicoValor = new PdfPCell(new Phrase("", F_CABECALHO_VALOR));
        cellAcademicoValor.setBorder(Rectangle.BOX);
        cellAcademicoValor.setBorderWidth(1.5f);
        cellAcademicoValor.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellAcademicoValor.setPadding(5f);
        cellAcademicoValor.setMinimumHeight(25f);
        table.addCell(cellAcademicoValor);

        // Linha 2: Curso e Período
        PdfPCell cellCursoLabel = new PdfPCell(new Phrase("Curso", F_CABECALHO_LABEL));
        cellCursoLabel.setBorder(Rectangle.BOX);
        cellCursoLabel.setBorderWidth(1.5f);
        cellCursoLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellCursoLabel.setPadding(5f);
        table.addCell(cellCursoLabel);

        // Subcélula para Curso + Período
        PdfPTable subTableCurso = new PdfPTable(2);
        subTableCurso.setWidthPercentage(100);
        subTableCurso.setWidths(new float[]{4f, 1f});

        PdfPCell cellCursoValor = new PdfPCell(new Phrase(nomeTurma, F_CABECALHO_VALOR));
        cellCursoValor.setBorder(Rectangle.NO_BORDER);
        cellCursoValor.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellCursoValor.setPadding(5f);
        subTableCurso.addCell(cellCursoValor);

        PdfPTable periodoTable = new PdfPTable(2);
        periodoTable.setWidthPercentage(100);

        PdfPCell cellPeriodoLabel = new PdfPCell(new Phrase("Período", F_CABECALHO_LABEL));
        cellPeriodoLabel.setBorder(Rectangle.NO_BORDER);
        cellPeriodoLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellPeriodoLabel.setPadding(2f);
        periodoTable.addCell(cellPeriodoLabel);

        PdfPCell cellPeriodoValor = new PdfPCell(new Phrase("", F_CABECALHO_VALOR));
        cellPeriodoValor.setBorder(Rectangle.BOX);
        cellPeriodoValor.setBorderWidth(1.5f);
        cellPeriodoValor.setMinimumHeight(20f);
        cellPeriodoValor.setPadding(5f);
        periodoTable.addCell(cellPeriodoValor);

        PdfPCell wrapperPeriodo = new PdfPCell(periodoTable);
        wrapperPeriodo.setBorder(Rectangle.NO_BORDER);
        subTableCurso.addCell(wrapperPeriodo);

        PdfPCell cellCursoWrapper = new PdfPCell(subTableCurso);
        cellCursoWrapper.setBorder(Rectangle.BOX);
        cellCursoWrapper.setBorderWidth(1.5f);
        cellCursoWrapper.setPadding(0);
        table.addCell(cellCursoWrapper);

        // Linha 3: Disciplina
        PdfPCell cellDisciplinaLabel = new PdfPCell(new Phrase("Disciplina", F_CABECALHO_LABEL));
        cellDisciplinaLabel.setBorder(Rectangle.BOX);
        cellDisciplinaLabel.setBorderWidth(1.5f);
        cellDisciplinaLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellDisciplinaLabel.setPadding(5f);
        table.addCell(cellDisciplinaLabel);

        PdfPCell cellDisciplinaValor = new PdfPCell(new Phrase(prova.getDisciplina().getNome(), F_CABECALHO_VALOR));
        cellDisciplinaValor.setBorder(Rectangle.BOX);
        cellDisciplinaValor.setBorderWidth(1.5f);
        cellDisciplinaValor.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellDisciplinaValor.setPadding(5f);
        table.addCell(cellDisciplinaValor);

        // Linha 4: Professor
        PdfPCell cellProfessorLabel = new PdfPCell(new Phrase("Professor", F_CABECALHO_LABEL));
        cellProfessorLabel.setBorder(Rectangle.BOX);
        cellProfessorLabel.setBorderWidth(1.5f);
        cellProfessorLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellProfessorLabel.setPadding(5f);
        table.addCell(cellProfessorLabel);

        PdfPCell cellProfessorValor = new PdfPCell(new Phrase(prova.getDisciplina().getProfessor().getUsuario().getNome(), F_CABECALHO_VALOR));
        cellProfessorValor.setBorder(Rectangle.BOX);
        cellProfessorValor.setBorderWidth(1.5f);
        cellProfessorValor.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellProfessorValor.setPadding(5f);
        table.addCell(cellProfessorValor);

        // Linha 5: Título da Atividade
        PdfPCell cellTituloLabel = new PdfPCell(new Phrase(prova.getTitulo().toUpperCase(), F_TITULO));
        cellTituloLabel.setBorder(Rectangle.BOX);
        cellTituloLabel.setBorderWidth(1.5f);
        cellTituloLabel.setColspan(2);
        cellTituloLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTituloLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTituloLabel.setPadding(8f);
        table.addCell(cellTituloLabel);

        return table;
    }

    /**
     * Classe interna para desenhar o círculo vetorial (VAZIO)
     */
    private static class CircleCellEvent implements PdfPCellEvent {
        private final float yPosition;
        private final float radius;

        public CircleCellEvent(float yPosition, float radius) {
            this.yPosition = yPosition;
            this.radius = radius;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            canvas.setColorStroke(Color.BLACK);
            canvas.setLineWidth(1f);

            float centerX = position.getLeft() + (position.getWidth() / 2);
            float centerY = position.getTop() - yPosition;

            canvas.circle(centerX, centerY, radius);
            canvas.stroke();
        }
    }

    /**
     * MÉTODO ATUALIZADO: Gabarito horizontal compacto (estilo múltiplas colunas)
     */
    private PdfPTable criarBlocoGabaritoCirculos(List<Questao> questoes) throws DocumentException {
        // Determinar quantas questões cabem por coluna (10 questões por coluna)
        int questoesPorColuna = 10;
        int numColunas = (int) Math.ceil((double) questoes.size() / questoesPorColuna);

        // Wrapper para centralizar
        PdfPTable wrapperTable = new PdfPTable(1);
        wrapperTable.setWidthPercentage(100);
        wrapperTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Tabela principal com múltiplas colunas de gabarito (reduzida para 75%)
        PdfPTable mainTable = new PdfPTable(numColunas);
        mainTable.setWidthPercentage(75);
        mainTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.setSpacingBefore(5);
        mainTable.setSpacingAfter(5);

        // Criar cada coluna de gabarito
        for (int col = 0; col < numColunas; col++) {
            int inicio = col * questoesPorColuna;
            int fim = Math.min(inicio + questoesPorColuna, questoes.size());

            PdfPCell cellColuna = new PdfPCell();
            cellColuna.setBorder(Rectangle.BOX);
            cellColuna.setBorderWidth(1.5f);
            cellColuna.setPadding(5);

            // Título do gabarito
            Paragraph titulo = new Paragraph("GABARITO", F_GABARITO_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(5);
            cellColuna.addElement(titulo);

            // Tabela de gabarito (6 colunas: Nº + A B C D E)
            PdfPTable gabaritoTable = new PdfPTable(6);
            gabaritoTable.setWidthPercentage(100);
            gabaritoTable.setWidths(new float[]{0.6f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f});
            gabaritoTable.setSpacingBefore(5);

            // Cabeçalho
            String[] headers = {"", "A", "B", "C", "D", "E"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, F_GABARITO_HEADER));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setBorder(Rectangle.NO_BORDER);
                headerCell.setPadding(1);
                headerCell.setMinimumHeight(10f);
                gabaritoTable.addCell(headerCell);
            }

            // Linhas de questões
            for (int i = inicio; i < fim; i++) {
                Questao questao = questoes.get(i);

                // Número da questão
                PdfPCell cellNum = new PdfPCell(new Phrase(questao.getNumero(), F_GABARITO_NUM));
                cellNum.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellNum.setBorder(Rectangle.BOX);
                cellNum.setBorderWidth(0.5f);
                cellNum.setPadding(1);
                cellNum.setMinimumHeight(15f);
                gabaritoTable.addCell(cellNum);

                // Círculos para cada alternativa
                String[] letras = {"A", "B", "C", "D", "E"};
                for (String letra : letras) {
                    PdfPCell cellCirculo = new PdfPCell();
                    cellCirculo.setBorder(Rectangle.BOX);
                    cellCirculo.setBorderWidth(0.5f);
                    cellCirculo.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cellCirculo.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cellCirculo.setPadding(1);
                    cellCirculo.setMinimumHeight(15f);

                    CircleCellEvent circleEvent = new CircleCellEvent(7.5f, 3.5f);
                    cellCirculo.setCellEvent(circleEvent);
                    gabaritoTable.addCell(cellCirculo);
                }
            }

            cellColuna.addElement(gabaritoTable);
            mainTable.addCell(cellColuna);
        }

        // Adicionar a mainTable dentro do wrapper para centralização
        PdfPCell wrapperCell = new PdfPCell(mainTable);
        wrapperCell.setBorder(Rectangle.NO_BORDER);
        wrapperCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        wrapperTable.addCell(wrapperCell);

        return wrapperTable;
    }

    private PdfPTable criarBlocoQuestao(Questao questao) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setKeepTogether(true);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph pEnunciado = new Paragraph(questao.getNumero() + ". " + questao.getEnunciado(), F_QUESTAO);
        pEnunciado.setSpacingAfter(8f);
        cell.addElement(pEnunciado);

        cell.addElement(criarParagrafoAlternativa("A) " + questao.getAlternativaA()));
        cell.addElement(criarParagrafoAlternativa("B) " + questao.getAlternativaB()));
        cell.addElement(criarParagrafoAlternativa("C) " + questao.getAlternativaC()));
        cell.addElement(criarParagrafoAlternativa("D) " + questao.getAlternativaD()));
        cell.addElement(criarParagrafoAlternativa("E) " + questao.getAlternativaE()));

        wrapper.addCell(cell);
        return wrapper;
    }

    private Paragraph criarParagrafoAlternativa(String texto) {
        Paragraph p = new Paragraph(texto, F_ALTERNATIVA);
        p.setIndentationLeft(15f);
        p.setSpacingAfter(4f);
        return p;
    }

    private PdfPCell criarCelulaCabecalho(String texto, Font fonte, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fonte));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(4f);
        return cell;
    }
}