package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.ProfileDtos.SocialAgentResponse;
import com.csi.profile.application.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API REST de consultation du personnel social.
 */
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class SocialAgentController {
    private final ProfileService profileService;

    @Operation(summary = "Lister les agents sociaux")
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<Page<SocialAgentResponse>> list(Pageable pageable) {
        return ApiResponse.success("Agents sociaux", profileService.findAgents(pageable));
    }
}
