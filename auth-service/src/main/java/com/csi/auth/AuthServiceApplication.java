package com.csi.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d entree du service d authentification et de gestion des comptes applicatifs.
 */
@SpringBootApplication(scanBasePackages = {"com.csi.auth", "com.csi.common"})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
