package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.SettingsDtos.*;
import com.csi.profile.application.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API du module parametres metier.
 */
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @Operation(summary = "Lister toutes les categories de parametres")
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<List<SettingsCategoryResponse>> all() {
        return ApiResponse.success("Parametres", settingsService.all());
    }

    @Operation(summary = "Lister les parametres d'une categorie")
    @GetMapping("/{category}")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<SettingsCategoryResponse> category(@PathVariable String category) {
        return ApiResponse.success("Parametres", settingsService.getCategory(category));
    }

    @Operation(summary = "Creer ou mettre a jour un parametre")
    @PutMapping("/{category}")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<SettingResponse> upsert(@PathVariable String category, @Valid @RequestBody UpsertSettingRequest request) {
        return ApiResponse.success("Parametre enregistre", settingsService.upsert(category, request));
    }
}
