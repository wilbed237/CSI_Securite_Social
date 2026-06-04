package com.csi.auth.infrastructure.persistence;

import com.csi.auth.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistance Spring Data pour rechercher et stocker les comptes utilisateurs.
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmailIgnoreCaseOrPhoneNumberOrUsernameIgnoreCase(String email, String phoneNumber, String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
}
