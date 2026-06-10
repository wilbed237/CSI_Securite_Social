package com.csi.reimbursement.application.service;

import com.csi.reimbursement.domain.model.Reimbursement;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Génère un justificatif de remboursement professionnel au format PDF.
 */
@Service
@Slf4j
public class ReceiptPdfService {

    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SUCCESS_COLOR = new Color(22, 163, 74);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(Reimbursement reimbursement, String maskedIban) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 60, 60, 70, 70);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new FooterPageEvent(reimbursement));
            doc.open();

            addHeader(doc, reimbursement);
            doc.add(Chunk.NEWLINE);
            addBeneficiaryBlock(doc, reimbursement);
            doc.add(Chunk.NEWLINE);
            addFinancialTable(doc, reimbursement);
            doc.add(Chunk.NEWLINE);
            addPaymentBlock(doc, reimbursement, maskedIban);
            doc.add(Chunk.NEWLINE);
            addValidationBlock(doc, reimbursement);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération PDF justificatif {}", reimbursement.getReimbursementNumber(), e);
            throw new RuntimeException("Impossible de générer le justificatif", e);
        }
    }

    private void addHeader(Document doc, Reimbursement r) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.4f, 1f});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(14);
        logoCell.setBackgroundColor(PRIMARY_COLOR);

        Font logoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.WHITE);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(186, 230, 253));
        Paragraph logoPara = new Paragraph();
        logoPara.add(new Phrase("Care Health\n", logoFont));
        logoPara.add(new Phrase("Clinical Suite", subFont));
        logoCell.addElement(logoPara);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(14);
        infoCell.setBackgroundColor(new Color(239, 246, 255));
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, PRIMARY_COLOR);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font metaBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);

        Paragraph infoPara = new Paragraph();
        infoPara.setAlignment(Element.ALIGN_RIGHT);
        infoPara.add(new Phrase("JUSTIFICATIF DE REMBOURSEMENT\n\n", titleFont));
        infoPara.add(new Phrase("Référence : ", metaFont));
        infoPara.add(new Phrase(r.getReimbursementNumber() + "\n", metaBoldFont));
        infoPara.add(new Phrase("Feuille de maladie : ", metaFont));
        infoPara.add(new Phrase(r.getSheetNumber() + "\n", metaBoldFont));
        infoPara.add(new Phrase("Date : ", metaFont));
        infoPara.add(new Phrase(r.getDate() != null ? r.getDate().format(DATE_FMT) : LocalDate.now().format(DATE_FMT), metaBoldFont));
        infoCell.addElement(infoPara);

        header.addCell(logoCell);
        header.addCell(infoCell);
        doc.add(header);
    }

    private void addBeneficiaryBlock(Document doc, Reimbursement r) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);

        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBackgroundColor(LIGHT_BG);
        cell.setBorderColor(BORDER_COLOR);

        Paragraph content = new Paragraph();
        content.add(new Phrase("INFORMATIONS DU DOSSIER\n\n", sectionFont));
        content.add(new Phrase("Feuille de maladie N° ", labelFont));
        content.add(new Phrase(r.getSheetNumber() + "\n", valueFont));
        content.add(new Phrase("Type de remboursement : ", labelFont));
        content.add(new Phrase((r.getReimbursementType() != null ? r.getReimbursementType().name().replaceAll("_", " ") : "—") + "\n", valueFont));
        content.add(new Phrase("Règle appliquée : ", labelFont));
        content.add(new Phrase((r.getRuleCode() != null ? r.getRuleCode() : "—") + "\n", valueFont));
        cell.addElement(content);

        block.addCell(cell);
        doc.add(block);
    }

    private void addFinancialTable(Document doc, Reimbursement r) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK);
        Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, SUCCESS_COLOR);

        Paragraph title = new Paragraph("DÉTAIL FINANCIER\n", sectionFont);
        title.setSpacingAfter(6);
        doc.add(title);

        PdfPTable table = new PdfPTable(new float[]{3f, 2f});
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addTableHeader(table, "Description", headerFont);
        addTableHeader(table, "Montant", headerFont);

        addTableRow(table, "Montant de base", formatMoney(r.getBaseAmount() != null ? r.getBaseAmount().doubleValue() : 0), cellFont, boldCellFont);
        addTableRow(table, "Montant éligible", formatMoney(r.getEligibleAmount() != null ? r.getEligibleAmount().doubleValue() : 0), cellFont, boldCellFont);
        addTableRow(table, "Taux appliqué", r.getRate() != null ? (r.getRate().multiply(new java.math.BigDecimal(100)).stripTrailingZeros().toPlainString() + " %") : "—", cellFont, boldCellFont);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Montant remboursé", totalFont));
        totalLabelCell.setPadding(8);
        totalLabelCell.setBackgroundColor(new Color(240, 253, 244));
        totalLabelCell.setBorderColor(new Color(187, 247, 208));
        PdfPCell totalValueCell = new PdfPCell(new Phrase(formatMoney(r.getReimbursedAmount() != null ? r.getReimbursedAmount().doubleValue() : 0), totalFont));
        totalValueCell.setPadding(8);
        totalValueCell.setBackgroundColor(new Color(240, 253, 244));
        totalValueCell.setBorderColor(new Color(187, 247, 208));
        table.addCell(totalLabelCell);
        table.addCell(totalValueCell);

        doc.add(table);
    }

    private void addPaymentBlock(Document doc, Reimbursement r, String maskedIban) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);

        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(BORDER_COLOR);

        Paragraph content = new Paragraph();
        content.add(new Phrase("PAIEMENT\n\n", sectionFont));

        content.add(new Phrase("Mode de paiement : ", labelFont));
        String paymentLabel = r.getPaymentType() != null
                ? (r.getPaymentType().name().equals("CASH") ? "Espèces" : "Virement bancaire")
                : "—";
        content.add(new Phrase(paymentLabel + "\n", valueFont));

        if (maskedIban != null && !maskedIban.isBlank()) {
            content.add(new Phrase("Compte : ", labelFont));
            content.add(new Phrase(maskedIban + "\n", valueFont));
        }

        if (r.getPaymentReference() != null && !r.getPaymentReference().isBlank()) {
            content.add(new Phrase("Référence de transaction : ", labelFont));
            content.add(new Phrase(r.getPaymentReference() + "\n", valueFont));
        }

        if (r.getProcessedAt() != null) {
            content.add(new Phrase("Date du paiement : ", labelFont));
            content.add(new Phrase(r.getProcessedAt().toString() + "\n", valueFont));
        }

        cell.addElement(content);
        block.addCell(cell);
        doc.add(block);
    }

    private void addValidationBlock(Document doc, Reimbursement r) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SUCCESS_COLOR);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, SUCCESS_COLOR);

        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBackgroundColor(new Color(240, 253, 244));
        cell.setBorderColor(new Color(187, 247, 208));

        Paragraph content = new Paragraph();
        content.add(new Phrase("VALIDATION\n\n", sectionFont));
        content.add(new Phrase("Statut : ", labelFont));
        content.add(new Phrase(r.getStatus() != null ? r.getStatus().name() : "—", statusFont));
        content.add(new Phrase("\n"));

        if (r.getApprovedByUserId() != null) {
            content.add(new Phrase("Approuvé par : ", labelFont));
            content.add(new Phrase(r.getApprovedByUserId().toString() + "\n", valueFont));
        }
        if (r.getProcessedByUserId() != null) {
            content.add(new Phrase("Traité par : ", labelFont));
            content.add(new Phrase(r.getProcessedByUserId().toString() + "\n", valueFont));
        }
        content.add(new Phrase("\n✓ Ce document constitue la preuve officielle de remboursement Care Health.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, TEXT_MUTED)));

        cell.addElement(content);
        block.addCell(cell);
        doc.add(block);
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(7);
        cell.setBorderColor(PRIMARY_COLOR);
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(7);
        labelCell.setBackgroundColor(LIGHT_BG);
        labelCell.setBorderColor(BORDER_COLOR);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(7);
        valueCell.setBorderColor(BORDER_COLOR);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatMoney(double amount) {
        return String.format(Locale.FRANCE, "%,.0f FCFA", amount);
    }

    private static class FooterPageEvent extends PdfPageEventHelper {
        private final Reimbursement reimbursement;
        FooterPageEvent(Reimbursement reimbursement) { this.reimbursement = reimbursement; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(148, 163, 184));
            String footer = String.format("Care Health | Justificatif N° %s | %s | Document confidentiel | Page %d",
                    reimbursement.getReimbursementNumber(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    writer.getPageNumber());
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(footer, footerFont),
                    (document.left() + document.right()) / 2, document.bottom() - 15, 0);
        }
    }
}
