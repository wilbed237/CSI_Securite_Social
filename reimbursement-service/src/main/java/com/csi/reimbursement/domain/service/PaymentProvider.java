package com.csi.reimbursement.domain.service;

import com.csi.reimbursement.domain.model.PaymentType;
import java.math.BigDecimal;

public interface PaymentProvider {
    boolean supports(PaymentType paymentType);
    String execute(String account, BigDecimal amount, String reimbursementReference);
}
