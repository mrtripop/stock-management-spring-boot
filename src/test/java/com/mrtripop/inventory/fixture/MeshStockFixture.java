package com.mrtripop.inventory.fixture;

import com.mrtripop.inventory.models.dto.MeshStockDto;
import java.util.UUID;

public final class MeshStockFixture {

  private MeshStockFixture() {}

  public static final UUID STORE_ID_LOCAL = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID STORE_ID_REMOTE = UUID.fromString("00000000-0000-0000-0000-000000000002");
  public static final UUID BRAND_ID_TYLENOL = UUID.fromString("00000000-0000-0000-0000-000000000010");
  public static final UUID BRAND_ID_ADVIL = UUID.fromString("00000000-0000-0000-0000-000000000011");
  public static final String GENERIC_NAME_PARACETAMOL = "Paracetamol";
  public static final String BRAND_NAME_TYLENOL = "Tylenol";
  public static final String BRAND_NAME_ADVIL = "Advil";
  public static final String STORE_NAME_LOCAL = "Main Store";
  public static final String STORE_NAME_REMOTE = "Branch Store";

  public static MeshStockDto localStoreStock() {
    return MeshStockDto.builder()
        .storeId(STORE_ID_LOCAL)
        .storeName(STORE_NAME_LOCAL)
        .brandId(BRAND_ID_TYLENOL)
        .brandName(BRAND_NAME_TYLENOL)
        .genericName(GENERIC_NAME_PARACETAMOL)
        .totalQuantity(100L)
        .batchCount(2L)
        .build();
  }

  public static MeshStockDto remoteStoreStock() {
    return MeshStockDto.builder()
        .storeId(STORE_ID_REMOTE)
        .storeName(STORE_NAME_REMOTE)
        .brandId(BRAND_ID_TYLENOL)
        .brandName(BRAND_NAME_TYLENOL)
        .genericName(GENERIC_NAME_PARACETAMOL)
        .totalQuantity(50L)
        .batchCount(1L)
        .build();
  }

  public static MeshStockDto remoteStoreStockDifferentBrand() {
    return MeshStockDto.builder()
        .storeId(STORE_ID_REMOTE)
        .storeName(STORE_NAME_REMOTE)
        .brandId(BRAND_ID_ADVIL)
        .brandName(BRAND_NAME_ADVIL)
        .genericName("Ibuprofen")
        .totalQuantity(200L)
        .batchCount(3L)
        .build();
  }
}
