package com.mrtripop.inventory.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.dto.BatchDto;
import com.mrtripop.inventory.models.dto.StockEntryRequest;
import java.time.LocalDate;
import java.util.UUID;

public final class BatchFixture {

  private BatchFixture() {}

  public static BatchDto validBatchDto() {
    return BatchDto.builder()
        .id(1L)
        .brandId(UUID.randomUUID())
        .batchNumber("BATCH-001")
        .expiryDate(LocalDate.now().plusYears(1))
        .quantity(100L)
        .status(BatchStatus.AVAILABLE)
        .build();
  }

  public static Batch defaultBatch() {
    Molecule molecule = Molecule.builder().id(UUID.randomUUID()).genericName("Paracetamol").build();
    Brand brand = Brand.builder().id(UUID.randomUUID()).brandName("Tylenol").molecule(molecule).build();
    return Batch.builder()
        .id(1L)
        .brand(brand)
        .batchNumber("BATCH-001")
        .expiryDate(LocalDate.now().plusYears(1))
        .quantity(100L)
        .status(BatchStatus.AVAILABLE)
        .build();
  }

  public static StockEntryRequest validStockEntryRequest() {
    return StockEntryRequest.builder()
        .barcode("1234567890123")
        .batchNumber("BATCH-001")
        .expiryDate(LocalDate.now().plusYears(1))
        .quantity(100L)
        .storeId(UUID.randomUUID())
        .supplierReference("SUP-001")
        .manufacturerLotNumber("LOT-001")
        .storageConditions("Room temperature")
        .build();
  }
}