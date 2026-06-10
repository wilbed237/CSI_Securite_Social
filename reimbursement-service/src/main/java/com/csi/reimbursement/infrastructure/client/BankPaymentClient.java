package com.csi.reimbursement.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.csi.reimbursement.domain.service.PaymentProvider;
import com.csi.reimbursement.domain.model.PaymentType;

import java.math.BigDecimal;

/**
 * Adaptateur simulant l acteur Banque pour les virements bancaires.
 */
@Slf4j
@Component
public class BankPaymentClient implements PaymentProvider {
    public boolean supports(PaymentType paymentType) { return paymentType == PaymentType.BANK_TRANSFER; }

    public String execute(String iban, BigDecimal amount, String reference) {
        log.info("Simulated bank transfer reference={} iban={} amount={}", reference, maskIban(iban), amount);
        return "BANK-" + java.util.UUID.randomUUID();
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 4) {
            return "****";
        }
        return "****" + iban.substring(iban.length() - 4);
    }
}
