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
import java.util.List;

@Service
public class PdfService {

    // === FONTES P&B - OTIMIZADAS PARA OCR ===
    // Aumentando o tamanho das fontes para melhor legibilidade e precisão do OCR.
    private static final Font F_LOGO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK); // Reduzido para cabeçalho compacto
    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK); // Reduzido para cabeçalho compacto
    private static final Font F_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK); // Reduzido para cabeçalho compacto
    private static final Font F_VALOR = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK); // Reduzido para cabeçalho compacto
    private static final Font F_QUESTAO_NUM = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK); // Reduzido para manter proporção
    private static final Font F_QUESTAO_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK); // Reduzido para manter proporção
    private static final Font F_ALTERNATIVA = FontFactory.getFont(FontFactory.HELVETICA, 9f, Color.BLACK); // Reduzido para manter proporção

    // === FONTES GABARITO ===
    private static final Font F_GAB_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK); // Mantido grande para destaque do OCR
    private static final Font F_GAB_NUMERO = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK); // Reduzido para gabarito compacto
    private static final Font F_GAB_LETRA = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK); // Reduzido para gabarito compacto

    public ByteArrayInputStream gerarPdfDaProva(Prova prova) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Margens ligeiramente maiores para melhor enquadramento do celular
        Document document = new Document(PageSize.A4, 45, 45, 40, 40); // De 40, 40, 35, 35 para 45, 45, 40, 40

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(criarTituloPrincipal(prova));
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
            document.add(criarBlocoInformacoesCompacto(prova));
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10))); // Espaçamento maior

            document.add(criarGabaritoCompacto(prova.getQuestoes()));
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 12))); // Espaçamento maior

            LineSeparator separator = new LineSeparator(1f, 100f, Color.BLACK, Element.ALIGN_CENTER, -2);
            document.add(separator);
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10))); // Espaçamento maior

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

    private PdfPTable criarTituloPrincipal(Prova prova) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        Paragraph titulo = new Paragraph("EXAMES ESPECIAIS DE RECUPERAÇÃO", F_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);

        PdfPCell cell = new PdfPCell(titulo);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        table.addCell(cell);

        return table;
    }



    private PdfPTable criarBlocoInformacoesCompacto(Prova prova) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell wrapper = new PdfPCell();
        wrapper.setBorder(Rectangle.BOX);
        wrapper.setBorderColor(Color.BLACK);
        wrapper.setBorderWidth(0.5f); // Borda mais fina
        wrapper.setPadding(5); // Padding menor

        String nomeTurma = (prova.getDisciplina() != null && prova.getDisciplina().getTurma() != null)
                ? prova.getDisciplina().getTurma().getNome() : "";

        String nomeProfessor = prova.getDisciplina().getProfessor().getUsuario().getNome();
        String nomeDisciplina = prova.getDisciplina().getNome();

        // Tabela de 4 colunas para o layout compacto
        PdfPTable grid = new PdfPTable(4);
        grid.setWidthPercentage(100);
        grid.setWidths(new float[]{1.5f, 3.5f, 1.5f, 3.5f}); // Ajuste de proporção

        // Linha 1
        grid.addCell(criarCelulaLabelCompacta("Nome:"));
        grid.addCell(criarCelulaValorCompacta(""));
        grid.addCell(criarCelulaLabelCompacta("Turma:"));
        grid.addCell(criarCelulaValorCompacta(nomeTurma));

        // Linha 2
        grid.addCell(criarCelulaLabelCompacta("Disciplina:"));
        grid.addCell(criarCelulaValorCompacta(nomeDisciplina));
        grid.addCell(criarCelulaLabelCompacta("Professor(a):"));
        grid.addCell(criarCelulaValorCompacta(nomeProfessor));

        // Linha 3
        grid.addCell(criarCelulaLabelCompacta("Data:"));
        grid.addCell(criarCelulaValorCompacta("....../....../......"));
        grid.addCell(criarCelulaLabelCompacta("Valor da prova:"));


        wrapper.addElement(grid);
        table.addCell(wrapper);

        return table;
    }



    private PdfPCell criarCelulaLabelCompacta(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_LABEL));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(2);
        cell.setPaddingBottom(2);
        cell.setPaddingRight(5);
        return cell;
    }



    // Método original (não usado mais no bloco de informações)
    private PdfPCell criarCelulaValor(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_VALOR));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.8f);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingTop(3);
        cell.setPaddingBottom(5);
        cell.setPaddingLeft(6);
        cell.setMinimumHeight(20f);
        return cell;
    }

    // NOVO: Célula otimizada para escrita manual (sem linha de base)
    private PdfPCell criarCelulaValorCompacta(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, F_VALOR));
        cell.setBorder(Rectangle.BOTTOM); // Usando linha de base para o cabeçalho, pois não é o gabarito
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingTop(2);
        cell.setPaddingBottom(2);
        cell.setPaddingLeft(5);
        cell.setMinimumHeight(15f); // Altura mínima bem menor
        return cell;
    }



    /**
     * GABARITO COMPACTO - MODELO TRADICIONAL
     * Otimizado para OCR de celular.
     */
    private PdfPTable criarGabaritoCompacto(List<Questao> questoes) throws DocumentException {
        // Wrapper principal
        PdfPTable mainWrapper = new PdfPTable(1);
        mainWrapper.setWidthPercentage(100);
        mainWrapper.setSpacingBefore(8); // Espaçamento maior
        mainWrapper.setSpacingAfter(15); // Espaçamento maior

        // Título GABARITO
        Paragraph titulo = new Paragraph("GABARITO", F_GAB_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(8); // Espaçamento maior

        PdfPCell tituloCell = new PdfPCell(titulo);
        tituloCell.setBorder(Rectangle.NO_BORDER);
        tituloCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainWrapper.addCell(tituloCell);

        // Wrapper interno para centralizar a tabela
        PdfPTable innerWrapper = new PdfPTable(1);
        innerWrapper.setWidthPercentage(100);
        innerWrapper.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Tabela do gabarito - OTIMIZADA (25% da largura)
        PdfPTable gabaritoTable = new PdfPTable(6);
        gabaritoTable.setWidthPercentage(25); // Reduzido para 25% conforme a imagem de referência
        gabaritoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        gabaritoTable.setWidths(new float[]{1.0f, 1f, 1f, 1f, 1f, 1f}); // Mantendo a proporção 1:1 para as colunas de alternativa

        // Removendo Marcadores de Ancoragem para layout compacto (pode ser reintroduzido se o OCR falhar)
        // A centralização e o tamanho reduzido devem compensar a falta de âncoras.


        // Header da tabela (vazio, a, b, c, d, e)
        String[] headers = {"", "A", "B", "C", "D", "E"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, F_GAB_LETRA));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setBorder(Rectangle.BOX);
            headerCell.setBorderWidth(0.5f); // Borda mais fina
            headerCell.setPadding(3); // Padding menor
            headerCell.setMinimumHeight(14f); // Altura mínima menor
            gabaritoTable.addCell(headerCell);
        }

        // Linhas das questões
        for (Questao questao : questoes) {
            // Número da questão
            PdfPCell numCell = new PdfPCell(new Phrase(questao.getNumero(), F_GAB_NUMERO));
            numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            numCell.setBorder(Rectangle.BOX);
            numCell.setBorderWidth(0.5f); // Borda mais fina
            numCell.setPadding(3); // Padding menor
            numCell.setMinimumHeight(18f); // Altura mínima menor
            gabaritoTable.addCell(numCell);

            // 5 círculos (a, b, c, d, e)
            for (int i = 0; i < 5; i++) {
                PdfPCell circleCell = new PdfPCell();
                circleCell.setBorder(Rectangle.BOX);
                circleCell.setBorderWidth(0.5f); // Borda mais fina
                circleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                circleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                circleCell.setMinimumHeight(18f); // Altura mínima menor
                circleCell.setPadding(3); // Padding menor

                // Adiciona o círculo - Raio menor para gabarito compacto
                // Posição Y ajustada para centralizar verticalmente na nova altura mínima
                CircleEvent circleEvent = new CircleEvent(9f, 5f); // Raio e posição Y ajustados para o gabarito compacto
                circleCell.setCellEvent(circleEvent);

                gabaritoTable.addCell(circleCell);
            }
        }

        // Removido o bloco de âncoras inferiores.

        // Adiciona tabela do gabarito ao wrapper interno
        PdfPCell innerCell = new PdfPCell(gabaritoTable);
        innerCell.setBorder(Rectangle.NO_BORDER);
        innerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        innerWrapper.addCell(innerCell);

        // Adiciona wrapper interno ao main
        PdfPCell mainCell = new PdfPCell(innerWrapper);
        mainCell.setBorder(Rectangle.NO_BORDER);
        mainCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainWrapper.addCell(mainCell);

        return mainWrapper;
    }

    /**
     * Evento para desenhar círculo
     */
    private static class CircleEvent implements PdfPCellEvent {
        private final float yPosition;
        private final float radius;

        public CircleEvent(float yPosition, float radius) {
            this.yPosition = yPosition;
            this.radius = radius;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];

            if (canvas != null) {
                canvas.setColorStroke(Color.BLACK);
                canvas.setLineWidth(0.5f); // Linha mais fina para o gabarito compacto

                float centerX = position.getLeft() + (position.getWidth() / 2);
                // A posição Y é relativa ao topo da célula (position.getTop())
                float centerY = position.getTop() - yPosition;

                canvas.circle(centerX, centerY, radius);
                canvas.stroke();
            }
        }
    }

    private PdfPTable criarBlocoQuestao(Questao questao) {
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        wrapper.setKeepTogether(true);
        wrapper.setSpacingAfter(8f); // Espaçamento menor entre questões

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.5f); // Borda mais fina
        cell.setPadding(8f); // Padding interno menor

        Paragraph pNumero = new Paragraph();
        pNumero.add(new Chunk(questao.getNumero() + ". ", F_QUESTAO_NUM));
        pNumero.add(new Chunk(questao.getEnunciado(), F_QUESTAO_TEXTO));
        pNumero.setSpacingAfter(6f); // Espaçamento menor
        pNumero.setLeading(12f); // Espaçamento entre linhas menor
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

        Font fLetra = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, Color.BLACK); // Fonte ajustada
        p.add(new Chunk(letra + ") ", fLetra));
        p.add(new Chunk(texto, F_ALTERNATIVA));

        p.setIndentationLeft(18f); // Recuo menor
        p.setSpacingAfter(4f); // Espaçamento menor entre alternativas
        p.setLeading(11f); // Espaçamento entre linhas menor

        return p;
    }
}
