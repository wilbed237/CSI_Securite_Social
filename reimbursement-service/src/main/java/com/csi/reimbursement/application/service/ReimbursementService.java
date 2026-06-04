package com.csi.reimbursement.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.reimbursement.application.dto.ReimbursementDtos.*;
import com.csi.reimbursement.application.mapper.ReimbursementMapper;
import com.csi.reimbursement.domain.model.PaymentType;
import com.csi.reimbursement.domain.model.Reimbursement;
import com.csi.reimbursement.domain.service.ReimbursementCalculator;
import com.csi.reimbursement.infrastructure.client.BankPaymentClient;
import com.csi.reimbursement.infrastructure.client.MedicalClient;
import com.csi.reimbursement.infrastructure.persistence.ReimbursementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cas d utilisation de remboursement : controle la feuille, calcule le montant et execute le paiement.
 */
@Service
@RequiredArgsConstructor
public class ReimbursementService {
    private final ReimbursementRepository reimbursements;
    private final MedicalClient medicalClient;
    private final BankPaymentClient bankPaymentClient;
    private final ReimbursementCalculator calculator;
    private final ReimbursementMapper mapper;

    /**
     * Execute un remboursement unique pour une feuille de maladie completee.
     */
    @Transactional
    public ReimbursementResponse reimburse(CreateReimbursementRequest request) {
        if (reimbursements.existsBySheetNumberIgnoreCase(request.sheetNumber())) {
            throw new BusinessException("SHEET_ALREADY_REIMBURSED", "Cette feuille de maladie a deja ete remboursee");
        }
        if (request.paymentType() == PaymentType.BANK_TRANSFER && (request.bankIban() == null || request.bankIban().isBlank())) {
            throw new BusinessException("IBAN_REQUIRED", "L'IBAN est obligatoire pour un virement");
        }
        var sheet = medicalClient.getDiseaseSheet(request.sheetNumber());
        var calculation = calculator.calculate(sheet.doctorType(), sheet.consultationCost());
        String reference = "RB-" + UUID.randomUUID();
        if (request.paymentType() == PaymentType.BANK_TRANSFER) {
            bankPaymentClient.transfer(request.bankIban(), calculation.amount(), reference);
        }
        Reimbursement reimbursement = Reimbursement.builder()
                .reimbursementNumber(reference)
                .sheetNumber(request.sheetNumber())
                .paymentType(request.paymentType())
                .bankIban(request.bankIban())
                .baseAmount(sheet.consultationCost())
                .rate(calculation.rate())
                .reimbursedAmount(calculation.amount())
                .build();
        return mapper.toResponse(reimbursements.save(reimbursement));
    }

    @Transactional(readOnly = true)
    public ReimbursementResponse get(String reimbursementNumber) {
        return reimbursements.findByReimbursementNumberIgnoreCase(reimbursementNumber)
                .map(mapper::toResponse)
                .orElseThrow(() -> new BusinessException("REIMBURSEMENT_NOT_FOUND", "Remboursement introuvable"));
    }
}
