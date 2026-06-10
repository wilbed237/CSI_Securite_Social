package com.csi.reimbursement.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.reimbursement.application.dto.ReimbursementDtos.*;
import com.csi.reimbursement.application.service.ReimbursementService;
import com.csi.reimbursement.application.service.ReceiptPdfService;
import com.csi.reimbursement.config.AuthenticatedUser;
import com.csi.reimbursement.domain.model.ReimbursementStatus;
import com.csi.reimbursement.domain.model.ReimbursementType;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * API REST reservee aux agents pour executer et consulter les remboursements.
 */
@RestController
@RequestMapping("/api/v1/reimbursements")
@RequiredArgsConstructor
public class ReimbursementController {
    private final ReimbursementService reimbursementService;
    private final ReceiptPdfService receiptPdfService;

    @Operation(summary = "Effectuer un remboursement depuis une feuille de maladie")
    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<ReimbursementResponse>> reimburse(@Valid @RequestBody CreateReimbursementRequest request,
                                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Demande de remboursement creee", reimbursementService.create(request)));
    }

    @Operation(summary = "Lister les remboursements avec filtres")
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<Page<ReimbursementResponse>> list(
            @RequestParam(required = false) ReimbursementStatus status,
            @RequestParam(required = false) ReimbursementType type,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "false") boolean processedByCurrentUser,
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable) {
        UUID agentId = processedByCurrentUser ? user.userId() : null;
        return ApiResponse.success("Remboursements", reimbursementService.list(status, type, startDate, endDate, agentId, pageable));
    }

    @Operation(summary = "Consulter un remboursement par reference")
    @GetMapping("/{reimbursementNumber}")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<ReimbursementResponse> get(@PathVariable String reimbursementNumber) {
        return ApiResponse.success("Remboursement", reimbursementService.get(reimbursementNumber));
    }

    @PostMapping("/{reimbursementNumber}/calculate")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<ReimbursementResponse> calculate(@PathVariable String reimbursementNumber) {
        return ApiResponse.success("Remboursement calcule", reimbursementService.calculate(reimbursementNumber));
    }

    @PatchMapping("/{reimbursementNumber}/approve")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<ReimbursementResponse> approve(@PathVariable String reimbursementNumber, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success("Remboursement approuve", reimbursementService.approve(reimbursementNumber, user.userId()));
    }

    @PatchMapping("/{reimbursementNumber}/reject")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<ReimbursementResponse> reject(@PathVariable String reimbursementNumber,
            @Valid @RequestBody RejectReimbursementRequest request, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success("Remboursement rejete", reimbursementService.reject(reimbursementNumber, request, user.userId()));
    }

    @PostMapping("/{reimbursementNumber}/execute")
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ApiResponse<ReimbursementResponse> execute(@PathVariable String reimbursementNumber, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success("Remboursement execute", reimbursementService.execute(reimbursementNumber, user.userId()));
    }

    @Operation(summary = "Telecharger le justificatif de remboursement en PDF")
    @GetMapping(value = "/{reimbursementNumber}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('AGENT','AGENT_SOCIAL','SOCIAL_AGENT','SECURITY_AGENT','ADMIN')")
    public ResponseEntity<byte[]> receipt(@PathVariable String reimbursementNumber) {
        byte[] pdf = reimbursementService.generateReceipt(reimbursementNumber);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=justificatif-" + reimbursementNumber + ".pdf")
                .body(pdf);
    }
}
