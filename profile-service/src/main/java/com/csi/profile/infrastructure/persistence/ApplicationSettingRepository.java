package com.csi.profile.infrastructure.persistence;

import com.csi.profile.domain.model.ApplicationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository des parametres metier.
 */
public interface ApplicationSettingRepository extends JpaRepository<ApplicationSetting, UUID> {
    List<ApplicationSetting> findByCategoryIgnoreCaseOrderByKeyAsc(String category);
    Optional<ApplicationSetting> findByCategoryIgnoreCaseAndKeyIgnoreCase(String category, String key);
}
