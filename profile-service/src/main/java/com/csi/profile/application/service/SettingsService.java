package com.csi.profile.application.service;

import com.csi.profile.application.dto.SettingsDtos.*;
import com.csi.profile.domain.model.ApplicationSetting;
import com.csi.profile.infrastructure.persistence.ApplicationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Cas d'utilisation du module parametres metier.
 */
@Service
@RequiredArgsConstructor
public class SettingsService {
    private final ApplicationSettingRepository repository;

    @Transactional(readOnly = true)
    public SettingsCategoryResponse getCategory(String category) {
        return new SettingsCategoryResponse(normalize(category), repository.findByCategoryIgnoreCaseOrderByKeyAsc(category).stream().map(this::toResponse).toList());
    }

    @Transactional
    public SettingResponse upsert(String category, UpsertSettingRequest request) {
        String normalizedCategory = normalize(category);
        String normalizedKey = normalize(request.key());
        ApplicationSetting setting = repository.findByCategoryIgnoreCaseAndKeyIgnoreCase(normalizedCategory, normalizedKey)
                .orElseGet(() -> ApplicationSetting.builder().category(normalizedCategory).key(normalizedKey).build());
        setting.setValue(request.value());
        setting.setDescription(request.description());
        setting.setActive(request.active());
        setting.setUpdatedAt(Instant.now());
        return toResponse(repository.save(setting));
    }

    @Transactional(readOnly = true)
    public List<SettingsCategoryResponse> all() {
        return List.of("GENERAL", "MEDICAL", "INSURANCE", "SECURITY").stream()
                .map(this::getCategory)
                .toList();
    }

    private SettingResponse toResponse(ApplicationSetting setting) {
        return new SettingResponse(setting.getId(), setting.getCategory(), setting.getKey(), setting.getValue(), setting.getDescription(), setting.isActive());
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
