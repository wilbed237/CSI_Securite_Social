package com.csi.reimbursement.infrastructure.client;

import com.csi.reimbursement.domain.model.PaymentType;
import com.csi.reimbursement.domain.service.PaymentProvider;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CashPaymentProvider implements PaymentProvider {
    public boolean supports(PaymentType paymentType) { return paymentType == PaymentType.CASH; }
    public String execute(String account, BigDecimal amount, String reference) { return "CASH-" + UUID.randomUUID(); }
}
