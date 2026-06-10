package com.csi.reimbursement.application.service;

import com.csi.common.domain.BusinessException;
import com.csi.reimbursement.application.dto.ReimbursementDtos.*;
import com.csi.reimbursement.domain.model.*;
import com.csi.reimbursement.domain.service.PaymentProvider;
import com.csi.reimbursement.domain.service.ReimbursementCalculator;
import com.csi.reimbursement.infrastructure.client.MedicalClient;
import com.csi.reimbursement.infrastructure.persistence.ReimbursementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReimbursementService {
    private final ReimbursementRepository reimbursements;
    private final MedicalClient medicalClient;
    private final ReimbursementCalculator calculator;
    private final SensitiveDataCipher cipher;
    private final List<PaymentProvider> paymentProviders;
    private final ReceiptPdfService receiptPdfService;

    @Transactional
    public ReimbursementResponse create(CreateReimbursementRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existing = reimbursements.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) return toResponse(existing.get());
        }
        if (reimbursements.existsBySheetNumberIgnoreCase(request.sheetNumber())) {
            throw new BusinessException("SHEET_ALREADY_REGISTERED", "Une demande existe deja pour cette feuille de maladie");
        }
        validatePaymentData(request.paymentType(), request.bankIban());
        var sheet = medicalClient.getDiseaseSheet(request.sheetNumber());
        if (!List.of("SUBMITTED", "UNDER_REVIEW", "APPROVED").contains(sheet.status())) {
            throw new BusinessException("DISEASE_SHEET_NOT_SUBMITTED", "La feuille doit etre soumise avant le remboursement");
        }
        var calculation = calculator.calculate(sheet.doctorType(), sheet.consultationCost());
        Reimbursement reimbursement = Reimbursement.builder()
                .reimbursementNumber("RB-" + UUID.randomUUID()).sheetNumber(request.sheetNumber())
                .reimbursementType(request.reimbursementType() == null ? ReimbursementType.CONSULTATION : request.reimbursementType())
                .paymentType(request.paymentType()).bankIbanEncrypted(cipher.encrypt(request.bankIban()))
                .baseAmount(sheet.consultationCost()).eligibleAmount(sheet.consultationCost())
                .rate(calculation.rate()).reimbursedAmount(calculation.amount())
                .ruleCode("CONSULTATION_" + sheet.doctorType() + "_V1").calculatedAt(Instant.now())
                .status(ReimbursementStatus.PENDING).idempotencyKey(request.idempotencyKey()).build();
        return toResponse(reimbursements.save(reimbursement));
    }

    /** Alias conserve pour les anciens appels Java. La creation n'execute plus le paiement. */
    @Transactional
    public ReimbursementResponse reimburse(CreateReimbursementRequest request, UUID ignoredUserId) { return create(request); }

    @Transactional
    public ReimbursementResponse calculate(String number) {
        Reimbursement reimbursement = load(number);
        if (reimbursement.getStatus() != ReimbursementStatus.PENDING) {
            throw new BusinessException("REIMBURSEMENT_NOT_PENDING", "Seule une demande en attente peut etre recalculee");
        }
        var sheet = medicalClient.getDiseaseSheet(reimbursement.getSheetNumber());
        var calculation = calculator.calculate(sheet.doctorType(), sheet.consultationCost());
        reimbursement.setBaseAmount(sheet.consultationCost());
        reimbursement.setEligibleAmount(sheet.consultationCost());
        reimbursement.setRate(calculation.rate());
        reimbursement.setReimbursedAmount(calculation.amount());
        reimbursement.setRuleCode("CONSULTATION_" + sheet.doctorType() + "_V1");
        reimbursement.setCalculatedAt(Instant.now());
        return toResponse(reimbursements.save(reimbursement));
    }

    @Transactional
    public ReimbursementResponse approve(String number, UUID agentId) {
        Reimbursement reimbursement = load(number);
        requireStatus(reimbursement, ReimbursementStatus.PENDING);
        reimbursement.setStatus(ReimbursementStatus.APPROVED);
        reimbursement.setApprovedByUserId(agentId);
        reimbursement.setApprovedAt(Instant.now());
        reimbursement.setRejectionReason(null);
        return toResponse(reimbursements.save(reimbursement));
    }

    @Transactional
    public ReimbursementResponse reject(String number, RejectReimbursementRequest request, UUID agentId) {
        Reimbursement reimbursement = load(number);
        requireStatus(reimbursement, ReimbursementStatus.PENDING);
        reimbursement.setStatus(ReimbursementStatus.REJECTED);
        reimbursement.setProcessedByUserId(agentId);
        reimbursement.setProcessedAt(Instant.now());
        reimbursement.setRejectionReason(request.reason());
        return toResponse(reimbursements.save(reimbursement));
    }

    @Transactional
    public ReimbursementResponse execute(String number, UUID agentId) {
        Reimbursement reimbursement = load(number);
        requireStatus(reimbursement, ReimbursementStatus.APPROVED);
        if (reimbursement.getPaymentReference() != null) {
            throw new BusinessException("REIMBURSEMENT_ALREADY_EXECUTED", "Ce remboursement a deja une reference de paiement");
        }
        PaymentProvider provider = paymentProviders.stream().filter(p -> p.supports(reimbursement.getPaymentType())).findFirst()
                .orElseThrow(() -> new BusinessException("PAYMENT_PROVIDER_NOT_FOUND", "Aucun fournisseur de paiement disponible"));
        String account = cipher.decrypt(reimbursement.getBankIbanEncrypted());
        String paymentReference = provider.execute(account, reimbursement.getReimbursedAmount(), reimbursement.getReimbursementNumber());
        reimbursement.setPaymentReference(paymentReference);
        reimbursement.setStatus(ReimbursementStatus.EXECUTED);
        reimbursement.setProcessedByUserId(agentId);
        reimbursement.setProcessedAt(Instant.now());
        return toResponse(reimbursements.save(reimbursement));
    }

    @Transactional(readOnly = true)
    public ReimbursementResponse get(String number) { return toResponse(load(number)); }

    @Transactional(readOnly = true)
    public byte[] generateReceipt(String number) {
        Reimbursement reimbursement = load(number);
        if (reimbursement.getStatus() != ReimbursementStatus.EXECUTED) {
            throw new BusinessException("RECEIPT_NOT_AVAILABLE",
                    "Le justificatif n'est disponible que pour un remboursement execute (statut actuel : " + reimbursement.getStatus() + ")");
        }
        String maskedIban = mask(cipher.decrypt(reimbursement.getBankIbanEncrypted()));
        return receiptPdfService.generate(reimbursement, maskedIban);
    }

    @Transactional(readOnly = true)
    public Page<ReimbursementResponse> list(ReimbursementStatus status, ReimbursementType type, LocalDate startDate,
                                            LocalDate endDate, UUID processedByUserId, Pageable pageable) {
        Specification<Reimbursement> spec = Specification.where(null);
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (type != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("reimbursementType"), type));
        if (startDate != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate));
        if (endDate != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate));
        if (processedByUserId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("processedByUserId"), processedByUserId));
        return reimbursements.findAll(spec, pageable).map(this::toResponse);
    }

    private Reimbursement load(String number) {
        return reimbursements.findByReimbursementNumberIgnoreCase(number)
                .orElseThrow(() -> new BusinessException("REIMBURSEMENT_NOT_FOUND", "Remboursement introuvable"));
    }

    private void requireStatus(Reimbursement reimbursement, ReimbursementStatus expected) {
        if (reimbursement.getStatus() != expected) {
            throw new BusinessException("REIMBURSEMENT_TRANSITION_INVALID", "Transition de remboursement interdite depuis " + reimbursement.getStatus());
        }
    }

    private void validatePaymentData(PaymentType type, String iban) {
        if (type == PaymentType.BANK_TRANSFER && (iban == null || iban.isBlank())) {
            throw new BusinessException("IBAN_REQUIRED", "L'IBAN est obligatoire pour un virement");
        }
    }

    private ReimbursementResponse toResponse(Reimbursement r) {
        return new ReimbursementResponse(r.getId(), r.getReimbursementNumber(), r.getSheetNumber(), r.getDate(),
                r.getReimbursementType(), r.getPaymentType(), mask(cipher.decrypt(r.getBankIbanEncrypted())),
                r.getBaseAmount(), r.getEligibleAmount(), r.getRate(), r.getReimbursedAmount(), r.getRuleCode(),
                r.getStatus(), r.getApprovedByUserId(), r.getApprovedAt(), r.getProcessedByUserId(), r.getProcessedAt(),
                r.getPaymentReference(), r.getRejectionReason());
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) return null;
        return "****" + value.substring(Math.max(0, value.length() - 4));
    }
}
