package com.csi.profile.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * DTO du module parametres.
 */
public final class SettingsDtos {
    private SettingsDtos() {}

    public record SettingResponse(UUID id, String category, String key, String value, String description, boolean active) {}
    public record UpsertSettingRequest(
            @NotBlank @Size(max = 120) String key,
            @NotBlank @Size(max = 1000) String value,
            @Size(max = 240) String description,
            boolean active) {}
    public record SettingsCategoryResponse(String category, List<SettingResponse> settings) {}
}
