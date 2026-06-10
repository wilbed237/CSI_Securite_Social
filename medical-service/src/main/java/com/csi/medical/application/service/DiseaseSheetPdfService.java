package com.csi.medical.application.service;

import com.csi.medical.domain.model.DiseaseSheet;
import com.csi.medical.domain.model.Medication;
import com.csi.medical.domain.model.Prescription;
import com.csi.medical.domain.model.PrescriptionType;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Génère un PDF professionnel pour la feuille de maladie.
 */
@Service
@Slf4j
public class DiseaseSheetPdfService {

    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color HEADER_BG = new Color(239, 246, 255);
    private static final Color LIGHT_GRAY = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(DiseaseSheet sheet, List<Prescription> prescriptions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new FooterPageEvent(sheet));
            doc.open();

            addHeader(doc, sheet);
            addSeparator(doc);
            addDoctorPatientBlock(doc, sheet);
            addSeparator(doc);
            addConsultationBlock(doc, sheet);
            if (hasMedications(prescriptions)) {
                addSeparator(doc);
                addPrescriptionBlock(doc, prescriptions);
            }
            if (sheet.getStatus() != null && isFinalized(sheet)) {
                addSeparator(doc);
                addReimbursementBlock(doc, sheet);
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération PDF feuille de maladie {}", sheet.getSheetNumber(), e);
            throw new RuntimeException("Impossible de générer le PDF", e);
        }
    }

    private void addHeader(Document doc, DiseaseSheet sheet) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.5f, 1f});
        header.setSpacingAfter(0);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(12);
        logoCell.setBackgroundColor(PRIMARY_COLOR);

        Font logoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(186, 230, 253));

        Paragraph logoPara = new Paragraph();
        logoPara.add(new Phrase("Care Health\n", logoFont));
        logoPara.add(new Phrase("Clinical Suite", subFont));
        logoCell.addElement(logoPara);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(12);
        infoCell.setBackgroundColor(HEADER_BG);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, PRIMARY_COLOR);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font metaBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);

        Paragraph infoPara = new Paragraph();
        infoPara.setAlignment(Element.ALIGN_RIGHT);
        infoPara.add(new Phrase("FEUILLE DE MALADIE\n", titleFont));
        infoPara.add(new Phrase("\nN° ", metaFont));
        infoPara.add(new Phrase(sheet.getSheetNumber() + "\n", metaBoldFont));
        infoPara.add(new Phrase("Date : ", metaFont));
        infoPara.add(new Phrase(LocalDate.now().format(DATE_FMT), metaBoldFont));
        infoCell.addElement(infoPara);

        header.addCell(logoCell);
        header.addCell(infoCell);
        doc.add(header);
    }

    private void addDoctorPatientBlock(Document doc, DiseaseSheet sheet) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(12);
        table.setSpacingAfter(0);

        // Bloc médecin
        PdfPCell doctorCell = new PdfPCell();
        doctorCell.setBorderColor(BORDER_COLOR);
        doctorCell.setPadding(12);
        doctorCell.setBackgroundColor(LIGHT_GRAY);

        Paragraph doctorContent = new Paragraph();
        doctorContent.add(new Phrase("MÉDECIN TRAITANT\n\n", sectionFont));
        doctorContent.add(new Phrase("Matricule : ", labelFont));
        doctorContent.add(new Phrase(sheet.getConsultation().getDoctorMatricule() + "\n", valueFont));
        doctorContent.add(new Phrase("Type : ", labelFont));
        String typeLabel = "GENERALIST".equals(sheet.getConsultation().getDoctorType() != null ? sheet.getConsultation().getDoctorType().name() : "") ? "Médecin généraliste" : "Médecin spécialiste";
        doctorContent.add(new Phrase(typeLabel + "\n", valueFont));
        doctorContent.add(new Phrase("Date de consultation : ", labelFont));
        doctorContent.add(new Phrase(sheet.getConsultation().getStartedAt() != null ? sheet.getConsultation().getStartedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—", valueFont));
        doctorCell.addElement(doctorContent);

        // Bloc patient
        PdfPCell patientCell = new PdfPCell();
        patientCell.setBorderColor(BORDER_COLOR);
        patientCell.setPadding(12);
        patientCell.setBackgroundColor(LIGHT_GRAY);

        Paragraph patientContent = new Paragraph();
        patientContent.add(new Phrase("PATIENT COUVERT\n\n", sectionFont));
        patientContent.add(new Phrase("N° assuré : ", labelFont));
        patientContent.add(new Phrase(sheet.getConsultation().getInsuranceNumber() + "\n", valueFont));
        patientContent.add(new Phrase("Date de création : ", labelFont));
        patientContent.add(new Phrase(sheet.getDate() != null ? sheet.getDate().format(DATE_FMT) : "—", valueFont));
        patientCell.addElement(patientContent);

        table.addCell(doctorCell);
        table.addCell(patientCell);
        doc.add(table);
    }

    private void addConsultationBlock(Document doc, DiseaseSheet sheet) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK);

        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);
        block.setSpacingBefore(12);

        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(12);

        Paragraph content = new Paragraph();
        content.add(new Phrase("CONSULTATION\n\n", sectionFont));

        content.add(new Phrase("Motif : ", labelFont));
        content.add(new Phrase((sheet.getConsultation().getReason() != null ? sheet.getConsultation().getReason() : "—") + "\n", valueFont));

        content.add(new Phrase("\nDiagnostic / Conclusion :\n", labelFont));
        content.add(new Phrase((sheet.getDiagnosis() != null ? sheet.getDiagnosis() : "—") + "\n", textFont));

        if (sheet.getConsultation().getObservations() != null && !sheet.getConsultation().getObservations().isBlank()) {
            content.add(new Phrase("\nObservations :\n", labelFont));
            content.add(new Phrase(sheet.getConsultation().getObservations() + "\n", textFont));
        }

        if (sheet.getConsultation().getCost() != null) {
            content.add(new Phrase("\nMontant de la consultation : ", labelFont));
            content.add(new Phrase(formatMoney(sheet.getConsultation().getCost().doubleValue()), valueFont));
        }

        cell.addElement(content);
        block.addCell(cell);
        doc.add(block);
    }

    private void addPrescriptionBlock(Document doc, List<Prescription> prescriptions) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);

        Paragraph title = new Paragraph("ORDONNANCE\n", sectionFont);
        title.setSpacingBefore(12);
        doc.add(title);

        for (Prescription presc : prescriptions) {
            if (presc.getType() != PrescriptionType.MEDICATION || presc.getMedications().isEmpty()) continue;

            PdfPTable table = new PdfPTable(new float[]{2.5f, 1.5f, 1.5f, 1f, 1f, 2f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(6);

            String[] headers = {"Médicament", "Posologie", "Fréquence", "Durée", "Qté", "Instructions"};
            for (String h : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(h, headerFont));
                hCell.setBackgroundColor(PRIMARY_COLOR);
                hCell.setPadding(6);
                hCell.setBorderColor(PRIMARY_COLOR);
                table.addCell(hCell);
            }

            for (Medication med : presc.getMedications()) {
                table.addCell(styledCell(med.getName(), cellFont));
                table.addCell(styledCell(med.getPosology(), cellFont));
                table.addCell(styledCell(orDash(med.getFrequency()), cellFont));
                table.addCell(styledCell(orDash(med.getDuration()), cellFont));
                table.addCell(styledCell(med.getQuantity() != null ? String.valueOf(med.getQuantity()) : "—", cellFont));
                table.addCell(styledCell(orDash(med.getInstructions()), cellFont));
            }
            doc.add(table);
        }
    }

    private void addReimbursementBlock(Document doc, DiseaseSheet sheet) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);

        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);
        block.setSpacingBefore(12);

        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(new Color(187, 247, 208));
        cell.setBackgroundColor(new Color(240, 253, 244));
        cell.setPadding(12);

        Paragraph content = new Paragraph();
        content.add(new Phrase("REMBOURSEMENT\n\n", sectionFont));
        content.add(new Phrase("Statut : ", labelFont));
        content.add(new Phrase(sheet.getStatus().name() + "\n", valueFont));
        if (sheet.getReimbursementNumber() != null) {
            content.add(new Phrase("Référence : ", labelFont));
            content.add(new Phrase(sheet.getReimbursementNumber() + "\n", valueFont));
        }
        if (sheet.getPaymentType() != null) {
            content.add(new Phrase("Mode de paiement : ", labelFont));
            content.add(new Phrase(("CASH".equals(sheet.getPaymentType()) ? "Espèces" : "Virement bancaire") + "\n", valueFont));
        }
        if (sheet.getCompletedAt() != null) {
            content.add(new Phrase("Date de complétion : ", labelFont));
            content.add(new Phrase(sheet.getCompletedAt().toString() + "\n", valueFont));
        }
        cell.addElement(content);
        block.addCell(cell);
        doc.add(block);
    }

    private void addSeparator(Document doc) throws DocumentException {
        LineSeparator sep = new LineSeparator(0.5f, 100, BORDER_COLOR, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(sep));
    }

    private PdfPCell styledCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }

    private boolean hasMedications(List<Prescription> prescriptions) {
        return prescriptions.stream().anyMatch(p -> p.getType() == PrescriptionType.MEDICATION && !p.getMedications().isEmpty());
    }

    private boolean isFinalized(DiseaseSheet sheet) {
        return sheet.getStatus().name().equals("COMPLETED") || sheet.getStatus().name().equals("PAID");
    }

    private String orDash(String value) { return value == null || value.isBlank() ? "—" : value; }

    private String formatMoney(double amount) {
        return String.format(Locale.FRANCE, "%,.0f FCFA", amount);
    }

    private static class FooterPageEvent extends PdfPageEventHelper {
        private final DiseaseSheet sheet;
        FooterPageEvent(DiseaseSheet sheet) { this.sheet = sheet; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(148, 163, 184));
            String footer = String.format("Care Health | Feuille de maladie N° %s | Généré le %s | Document confidentiel — Usage médical uniquement | Page %d",
                    sheet.getSheetNumber(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), writer.getPageNumber());
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(footer, footerFont),
                    (document.left() + document.right()) / 2, document.bottom() - 15, 0);
        }
    }
}
