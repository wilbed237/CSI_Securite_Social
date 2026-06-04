package com.csi.reimbursement.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class BankPaymentClient {
    public void transfer(String iban, BigDecimal amount, String reference) {
        log.info("Simulated bank transfer reference={} iban={} amount={}", reference, maskIban(iban), amount);
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 4) {
            return "****";
        }
        return "****" + iban.substring(iban.length() - 4);
    }
}
