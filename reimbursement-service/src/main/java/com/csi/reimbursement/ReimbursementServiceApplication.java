package com.csi.reimbursement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d entree du service responsable du calcul et de l execution des remboursements.
 */
@SpringBootApplication(scanBasePackages = {"com.csi.reimbursement", "com.csi.common"})
public class ReimbursementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReimbursementServiceApplication.class, args);
    }
}
