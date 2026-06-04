package com.csi.reimbursement.domain.service;

import com.csi.common.domain.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ReimbursementCalculator {
    private static final BigDecimal GENERALIST_RATE = new BigDecimal("1.00");
    private static final BigDecimal SPECIALIST_RATE = new BigDecimal("0.80");

    public Calculation calculate(String doctorType, BigDecimal baseAmount) {
        BigDecimal rate = switch (doctorType) {
            case "GENERALIST" -> GENERALIST_RATE;
            case "SPECIALIST" -> SPECIALIST_RATE;
            default -> throw new BusinessException("UNKNOWN_DOCTOR_TYPE", "Type de medecin non supporte pour le remboursement");
        };
        return new Calculation(rate, baseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP));
    }

    public record Calculation(BigDecimal rate, BigDecimal amount) {}
}
