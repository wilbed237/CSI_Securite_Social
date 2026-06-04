package com.csi.reimbursement.presentation;

import com.csi.common.api.ApiResponse;
import com.csi.reimbursement.application.dto.ReimbursementDtos.*;
import com.csi.reimbursement.application.service.ReimbursementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reimbursements")
@RequiredArgsConstructor
public class ReimbursementController {
    private final ReimbursementService reimbursementService;

    @Operation(summary = "Effectuer un remboursement depuis une feuille de maladie")
    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<ReimbursementResponse>> reimburse(@Valid @RequestBody CreateReimbursementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Remboursement execute", reimbursementService.reimburse(request)));
    }

    @Operation(summary = "Consulter un remboursement par reference")
    @GetMapping("/{reimbursementNumber}")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<ReimbursementResponse> get(@PathVariable String reimbursementNumber) {
        return ApiResponse.success("Remboursement", reimbursementService.get(reimbursementNumber));
    }
}
