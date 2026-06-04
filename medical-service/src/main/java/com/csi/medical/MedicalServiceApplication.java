package com.csi.medical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d entree du service medical : consultations, prescriptions et feuilles de maladie.
 */
@SpringBootApplication(scanBasePackages = {"com.csi.medical", "com.csi.common"})
public class MedicalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalServiceApplication.class, args);
    }
}
