package com.mrtripop.inventory.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mrtripop.inventory.constant.SuccessCode;
import com.mrtripop.inventory.fixture.ReconciliationStatusFixture;
import com.mrtripop.inventory.models.dto.ReconciliationStatusDto;
import com.mrtripop.inventory.services.ReconciliationStatusService;
import com.mrtripop.inventory.services.StockReconciliationService;
import com.mrtripop.model.ResponseBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Admin Controller")
class InventoryAdminControllerTest {

  @Mock private StockReconciliationService reconciliationService;
  @Mock private ReconciliationStatusService statusService;

  @InjectMocks private InventoryAdminController controller;

  @Test
  @DisplayName("should trigger reconciliation and return success code")
  void shouldTriggerReconciliation() {
    // Act
    ResponseEntity<Object> response = controller.triggerReconciliation();

    // Assert
    verify(reconciliationService).reconcileAll();
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ResponseBody<?> body = (ResponseBody<?>) response.getBody();
    assertEquals(SuccessCode.INV2004_RECONCILE_ALL_SUCCESS.getCode(), body.getCode());
    assertEquals(SuccessCode.INV2004_RECONCILE_ALL_SUCCESS.getMessage(), body.getMessage());
  }

  @Test
  @DisplayName("should retrieve reconciliation status from status service")
  void shouldGetReconciliationStatus() {
    // Arrange
    ReconciliationStatusDto statusDto = ReconciliationStatusFixture.defaultDto();
    when(statusService.getStatus()).thenReturn(statusDto);

    // Act
    ResponseEntity<Object> response = controller.getReconciliationStatus();

    // Assert
    verify(statusService).getStatus();
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ResponseBody<?> body = (ResponseBody<?>) response.getBody();
    assertEquals(SuccessCode.INV2005_GET_RECONCILE_STATUS_SUCCESS.getCode(), body.getCode());
    assertEquals(statusDto, body.getData());
  }
}
