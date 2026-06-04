package com.csi.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d entree du service de gestion des assures, medecins et medecins traitants.
 */
@SpringBootApplication(scanBasePackages = {"com.csi.profile", "com.csi.common"})
public class ProfileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }
}
