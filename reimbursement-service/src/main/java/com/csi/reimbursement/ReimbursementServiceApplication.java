package com.csi.reimbursement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.csi.reimbursement", "com.csi.common"})
public class ReimbursementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReimbursementServiceApplication.class, args);
    }
}
