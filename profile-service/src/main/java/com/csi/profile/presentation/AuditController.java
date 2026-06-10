package com.csi.profile.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.profile.application.dto.ProfileDtos.AuditEventResponse;
import com.csi.profile.application.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-events")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<Page<AuditEventResponse>> list(Pageable pageable) {
        return ApiResponse.success("Journal d'audit", auditService.list(pageable));
    }
}
