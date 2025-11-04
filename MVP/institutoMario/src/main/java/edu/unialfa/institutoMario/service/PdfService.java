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

    // --- FONTES AUMENTADAS PARA OCR ---
    private static final Font F_GABARITO_NUM = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
    private static final Font F_GABARITO_LETRA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font F_GABARITO_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    // --- FIM DAS FONTES AUMENTADAS ---

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
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 8)));

            // (Bloco de ID já foi removido)

            // Gera o gabarito com círculos vazios (NOVO LAYOUT)
            document.add(criarBlocoGabaritoCirculos(prova.getQuestoes()));
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10)));

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
        // ... (Este método está correto, sem alterações)
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
        // ... (Este método está correto, sem alterações)
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 4f, 1f, 2f});

        String nomeTurma = (prova.getDisciplina() != null && prova.getDisciplina().getTurma() != null)
                ? prova.getDisciplina().getTurma().getNome() : "____________________";

        table.addCell(criarCelulaCabecalho("Aluno(a):", F_CABECALHO_LABEL, Element.ALIGN_LEFT));
        PdfPCell cellAlunoValor = criarCelulaCabecalho("________________________________________________________", F_CABECALHO_VALOR, Element.ALIGN_LEFT);
        cellAlunoValor.setColspan(3);
        table.addCell(cellAlunoValor);

        table.addCell(criarCelulaCabecalho("Professor(a):", F_CABECALHO_LABEL, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho(prova.getDisciplina().getProfessor().getUsuario().getNome(), F_CABECALHO_VALOR, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho("Turma:", F_CABECALHO_LABEL, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho(nomeTurma, F_CABECALHO_VALOR, Element.ALIGN_LEFT));

        table.addCell(criarCelulaCabecalho("Matéria:", F_CABECALHO_LABEL, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho(prova.getDisciplina().getNome(), F_CABECALHO_VALOR, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho("Data:", F_CABECALHO_LABEL, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho(prova.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), F_CABECALHO_VALOR, Element.ALIGN_LEFT));

        table.addCell(criarCelulaCabecalho("Nota:", F_CABECALHO_LABEL, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho("________", F_CABECALHO_VALOR, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho("", F_CABECALHO_LABEL, Element.ALIGN_LEFT));
        table.addCell(criarCelulaCabecalho("", F_CABECALHO_LABEL, Element.ALIGN_LEFT));

        return table;
    }

    /**
     * Classe interna para desenhar o círculo vetorial (VAZIO)
     * (Este método auxiliar é necessário para a nova tabela)
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
     * NOVO MÉTODO: Cria a célula do número da questão (ex: "1")
     */
    private PdfPCell criarCelulaQuestaoNum(String numeroQuestao) {
        PdfPCell cell = new PdfPCell(new Phrase(numeroQuestao, F_GABARITO_NUM)); // 16pt Bold
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(1.5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setMinimumHeight(50f); // Altura boa para a linha
        cell.setBackgroundColor(COR_FUNDO_CLARO); // Destaca a coluna do número
        return cell;
    }

    /**
     * NOVO MÉTODO: Cria a célula da alternativa (ex: "A" + Círculo)
     */
    private PdfPCell criarCelulaAlternativaComCirculo(String letra) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(1.5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setMinimumHeight(50f);

        // Tabela de empilhamento (para Letra e Círculo)
        PdfPTable stackingTable = new PdfPTable(1);
        stackingTable.setWidthPercentage(100);

        // 1. Célula da Letra
        Paragraph pLetra = new Paragraph(letra, F_GABARITO_LETRA); // 12pt Bold
        pLetra.setAlignment(Element.ALIGN_CENTER);
        PdfPCell cellLetra = new PdfPCell(pLetra);
        cellLetra.setBorder(Rectangle.NO_BORDER);
        cellLetra.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellLetra.setMinimumHeight(14f);
        stackingTable.addCell(cellLetra);

        // 2. Célula do Círculo
        PdfPCell cellCirculoVazio = new PdfPCell(new Phrase(" "));
        cellCirculoVazio.setBorder(Rectangle.NO_BORDER);
        cellCirculoVazio.setMinimumHeight(25f);
        // (Posição Y: 20, Raio: 8) -> Um bom tamanho para OCR
        CircleCellEvent circleEvent = new CircleCellEvent(20f, 8f);
        cellCirculoVazio.setCellEvent(circleEvent);
        stackingTable.addCell(cellCirculoVazio);

        cell.addElement(stackingTable);
        return cell;
    }

    /**
     * MÉTODO ATUALIZADO: Gabarito com layout de tabela horizontal
     */
    private PdfPTable criarBlocoGabaritoCirculos(List<Questao> questoes) throws DocumentException {
        PdfPTable wrapper = new PdfPTable(1); // Tabela principal (wrapper)
        wrapper.setWidthPercentage(100);

        // Célula do Título (GABARITO DE RESPOSTAS)
        PdfPCell titleCell = new PdfPCell(new Phrase("GABARITO DE RESPOSTAS", F_TITULO_SECAO));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.BOX);
        titleCell.setBorderWidth(2f);
        titleCell.setPadding(8);
        titleCell.setBackgroundColor(COR_FUNDO_CLARO);
        wrapper.addCell(titleCell);

        // Célula de Conteúdo (que segura a tabela do gabarito)
        PdfPCell contentCell = new PdfPCell();
        contentCell.setBorder(Rectangle.BOX);
        contentCell.setBorderWidth(2f);
        contentCell.setBorderWidthTop(0);
        contentCell.setPadding(10);

        // --- A NOVA TABELA DE GABARITO (6 Colunas) ---
        PdfPTable gabaritoTable = new PdfPTable(6);
        gabaritoTable.setWidthPercentage(100);
        // [Nº] [ A ] [ B ] [ C ] [ D ] [ E ]
        gabaritoTable.setWidths(new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f});

        // --- CABEÇALHO DA TABELA ---
        gabaritoTable.addCell(new PdfPCell(new Phrase("Nº", F_GABARITO_HEADER)));
        gabaritoTable.addCell(new PdfPCell(new Phrase("A", F_GABARITO_HEADER)));
        gabaritoTable.addCell(new PdfPCell(new Phrase("B", F_GABARITO_HEADER)));
        gabaritoTable.addCell(new PdfPCell(new Phrase("C", F_GABARITO_HEADER)));
        gabaritoTable.addCell(new PdfPCell(new Phrase("D", F_GABARITO_HEADER)));
        gabaritoTable.addCell(new PdfPCell(new Phrase("E", F_GABARITO_HEADER)));

        // Alinhar e colorir o cabeçalho
        for(PdfPCell c : gabaritoTable.getRow(0).getCells()) {
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(COR_FUNDO_CLARO);
            c.setPadding(5);
        }

        // --- LINHAS DAS QUESTÕES ---
        String[] letras = {"A", "B", "C", "D", "E"};
        for (Questao questao : questoes) {
            // Coluna 1: Número da Questão
            gabaritoTable.addCell(criarCelulaQuestaoNum(questao.getNumero()));

            // Colunas 2-6: Alternativas (A-E)
            for (String letra : letras) {
                gabaritoTable.addCell(criarCelulaAlternativaComCirculo(letra));
            }
        }
        // --- FIM DA NOVA TABELA ---

        contentCell.addElement(gabaritoTable);

        // Instrução (sem alteração)
        Paragraph instrucao = new Paragraph(
                "Preencha completely o círculo da alternativa correta.",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.ITALIC, Color.BLACK)
        );
        instrucao.setAlignment(Element.ALIGN_CENTER);
        instrucao.setSpacingBefore(10);
        contentCell.addElement(instrucao);

        wrapper.addCell(contentCell);
        return wrapper;
    }


    // --- MÉTODOS AUXILIARES (Sem alteração) ---

    private PdfPTable criarBlocoQuestao(Questao questao) {
        // ... (Este método está correto, sem alterações)
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
        // ... (Este método está correto, sem alterações)
        Paragraph p = new Paragraph(texto, F_ALTERNATIVA);
        p.setIndentationLeft(15f);
        p.setSpacingAfter(4f);
        return p;
    }

    private PdfPCell criarCelulaCabecalho(String texto, Font fonte, int alinhamento) {
        // ... (Este método está correto, sem alterações)
        PdfPCell cell = new PdfPCell(new Phrase(texto, fonte));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(4f);
        return cell;
    }
}