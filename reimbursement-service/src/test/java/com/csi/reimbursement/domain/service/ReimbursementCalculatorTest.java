package com.csi.reimbursement.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ReimbursementCalculatorTest {
    private final ReimbursementCalculator calculator = new ReimbursementCalculator();

    @Test
    void reimbursesGeneralistAtOneHundredPercent() {
        var result = calculator.calculate("GENERALIST", new BigDecimal("10000.00"));
        assertThat(result.rate()).isEqualByComparingTo("1.00");
        assertThat(result.amount()).isEqualByComparingTo("10000.00");
    }

    @Test
    void reimbursesSpecialistAtEightyPercent() {
        var result = calculator.calculate("SPECIALIST", new BigDecimal("10000.00"));
        assertThat(result.rate()).isEqualByComparingTo("0.80");
        assertThat(result.amount()).isEqualByComparingTo("8000.00");
    }
}
