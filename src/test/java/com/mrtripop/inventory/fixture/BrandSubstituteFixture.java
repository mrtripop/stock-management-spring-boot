package com.mrtripop.inventory.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.StoreStock;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class BrandSubstituteFixture {

  private BrandSubstituteFixture() {}

  public static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID MOLECULE_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
  public static final UUID REQUESTED_BRAND_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000010");
  public static final UUID PANADOL_BRAND_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000011");
  public static final UUID CALPOL_BRAND_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000012");

  public static final String STORE_NAME = "Main Pharmacy";
  public static final String OTHER_STORE_NAME = "Branch Pharmacy";
  public static final String GENERIC_NAME = "Paracetamol";
  public static final String OTHER_GENERIC_NAME = "Ibuprofen";
  public static final String REQUESTED_BRAND_NAME = "Tylenol";
  public static final String PANADOL_BRAND_NAME = "Panadol";
  public static final String CALPOL_BRAND_NAME = "Calpol";
  public static final String STRENGTH = "500mg";
  public static final String STRENGTH_DIFFERENT_CASE = "500MG";
  public static final String OTHER_STRENGTH = "250mg";
  public static final String FORM = "Tablet";
  public static final String OTHER_FORM = "Syrup";

  public static final String NEAR_BATCH_NUMBER = "BATCH-NEAR";
  public static final String MID_BATCH_NUMBER = "BATCH-MID";
  public static final String FAR_BATCH_NUMBER = "BATCH-FAR";
  public static final String EMPTY_BATCH_NUMBER = "BATCH-EMPTY";

  public static final BigDecimal PANADOL_PRICE = new BigDecimal("12.50");
  public static final BigDecimal CALPOL_PRICE = new BigDecimal("9.75");

  public static final Long PANADOL_NEAR_BATCH_ID = 101L;
  public static final Long PANADOL_FAR_BATCH_ID = 102L;
  public static final Long CALPOL_BATCH_ID = 201L;

  public static final Long PANADOL_NEAR_QUANTITY = 20L;
  public static final Long PANADOL_FAR_QUANTITY = 30L;
  public static final Long PANADOL_TOTAL_QUANTITY = 50L;
  public static final Long CALPOL_QUANTITY = 15L;

  public static LocalDate pastExpiryDate() {
    return LocalDate.now().minusDays(1);
  }

  public static LocalDate nearExpiryDate() {
    return LocalDate.now().plusMonths(1);
  }

  public static LocalDate midExpiryDate() {
    return LocalDate.now().plusMonths(6);
  }

  public static LocalDate farExpiryDate() {
    return LocalDate.now().plusYears(1);
  }

  public static Molecule molecule() {
    return Molecule.builder().id(MOLECULE_ID).genericName(GENERIC_NAME).build();
  }

  public static Brand requestedBrand() {
    return brand(REQUESTED_BRAND_ID, REQUESTED_BRAND_NAME, STRENGTH, FORM);
  }

  public static Brand requestedBrandWithoutStrength() {
    return brand(REQUESTED_BRAND_ID, REQUESTED_BRAND_NAME, null, FORM);
  }

  public static Brand requestedBrandWithoutForm() {
    return brand(REQUESTED_BRAND_ID, REQUESTED_BRAND_NAME, STRENGTH, null);
  }

  public static Brand panadolBrand() {
    return brand(PANADOL_BRAND_ID, PANADOL_BRAND_NAME, STRENGTH, FORM);
  }

  public static Brand calpolBrand() {
    return brand(CALPOL_BRAND_ID, CALPOL_BRAND_NAME, STRENGTH, FORM);
  }

  public static StoreStock stock(Brand brand, Long batchId, LocalDate expiryDate, Long quantity) {
    Batch batch =
        Batch.builder()
            .id(batchId)
            .brand(brand)
            .batchNumber(NEAR_BATCH_NUMBER)
            .expiryDate(expiryDate)
            .quantity(quantity)
            .status(BatchStatus.AVAILABLE)
            .build();
    Store store = Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).build();
    return StoreStock.builder().store(store).batch(batch).quantity(quantity).build();
  }

  public static StoreProduct storeProduct(Brand brand, BigDecimal price) {
    Store store = Store.builder().id(STORE_ID).name(STORE_NAME).type(StoreType.PHYSICAL).build();
    return StoreProduct.builder().store(store).brand(brand).price(price).isActive(true).build();
  }

  public static Molecule newMolecule(String genericName) {
    return Molecule.builder().genericName(genericName).build();
  }

  public static Brand newBrand(Molecule molecule, String brandName, String strength, String form) {
    return Brand.builder()
        .molecule(molecule)
        .brandName(brandName)
        .strength(strength)
        .form(form)
        .build();
  }

  public static Store newStore(String name) {
    return Store.builder().name(name).type(StoreType.PHYSICAL).build();
  }

  public static Batch newBatch(
      Brand brand, String batchNumber, LocalDate expiryDate, BatchStatus status) {
    return Batch.builder()
        .brand(brand)
        .batchNumber(batchNumber)
        .expiryDate(expiryDate)
        .quantity(PANADOL_TOTAL_QUANTITY)
        .status(status)
        .build();
  }

  public static StoreStock newStoreStock(Store store, Batch batch, Long quantity) {
    return StoreStock.builder().store(store).batch(batch).quantity(quantity).build();
  }

  public static StoreProduct newStoreProduct(
      Store store, Brand brand, BigDecimal price, boolean isActive) {
    return StoreProduct.builder()
        .store(store)
        .brand(brand)
        .price(price)
        .isActive(isActive)
        .build();
  }

  private static Brand brand(UUID id, String brandName, String strength, String form) {
    return Brand.builder()
        .id(id)
        .molecule(molecule())
        .brandName(brandName)
        .strength(strength)
        .form(form)
        .build();
  }
}
