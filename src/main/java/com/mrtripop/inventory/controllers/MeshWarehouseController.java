package com.mrtripop.inventory.controllers;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.dto.MeshStockResponseDto;
import com.mrtripop.inventory.services.MeshWarehouseService;
import com.mrtripop.model.ResponseBody;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mesh/stock")
@Validated
public class MeshWarehouseController {

  private final MeshWarehouseService meshWarehouseService;

  @GetMapping("/search")
  public ResponseEntity<Object> searchMeshStock(
      @RequestParam(required = false) UUID moleculeId,
      @RequestParam(required = false) String genericName,
      @RequestParam UUID requestingStoreId)
      throws ApplicationException {

    if (genericName != null) {
      genericName = genericName.trim();
    }

    if (moleculeId == null && (genericName == null || genericName.isBlank())) {
      throw new ApplicationException(ErrorCode.MESH_SEARCH_REQUIRES_PARAM, HttpStatus.BAD_REQUEST);
    }

    MeshStockResponseDto result;
    if (moleculeId != null) {
      result = meshWarehouseService.searchByMolecule(moleculeId, requestingStoreId);
    } else {
      result = meshWarehouseService.searchByGenericName(genericName, requestingStoreId);
    }

    return ResponseBody.builder()
        .code("INV2002")
        .message("Mesh stock search completed")
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}
