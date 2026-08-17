package com.mrtripop.inventory.controllers;

import com.mrtripop.inventory.constant.SuccessCode;
import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;
import com.mrtripop.inventory.services.ReconciliationStatusService;
import com.mrtripop.inventory.services.StockReconciliationService;
import com.mrtripop.model.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryAdminController {
    private final StockReconciliationService reconciliationService;
    private final ReconciliationStatusService statusService;

    @PostMapping("/reconcile")
    public ResponseEntity<Object> triggerReconciliation() {
        reconciliationService.reconcileAll();
        SuccessCode success = SuccessCode.INV2004_RECONCILE_ALL_SUCCESS;
        return ResponseBody.builder()
            .code(success.getCode())
            .message(success.getMessage())
            .build()
            .toResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/reconcile/status")
    public ResponseEntity<Object> getReconciliationStatus() {
        ReconciliationStatusDto status = statusService.getStatus();
        SuccessCode success = SuccessCode.INV2005_GET_RECONCILE_STATUS_SUCCESS;
        return ResponseBody.builder()
            .code(success.getCode())
            .message(success.getMessage())
            .data(status)
            .build()
            .toResponseEntity(HttpStatus.OK);
    }
}
