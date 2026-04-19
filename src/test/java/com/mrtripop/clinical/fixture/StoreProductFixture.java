package com.mrtripop.clinical.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.models.db.StoreType;
import java.math.BigDecimal;
import java.util.UUID;

public final class StoreProductFixture {

  private StoreProductFixture() {}

  public static StoreProduct defaultEntity(UUID storeId, UUID brandId) {
    Store store = Store.builder().id(storeId).name("Test Store").type(StoreType.PHYSICAL).build();
    Molecule molecule = Molecule.builder().id(UUID.randomUUID()).genericName("Paracetamol").build();
    Brand brand =
        Brand.builder()
            .id(brandId)
            .molecule(molecule)
            .brandName("Tylenol")
            .strength("500mg")
            .build();

    return StoreProduct.builder()
        .id(UUID.randomUUID())
        .store(store)
        .brand(brand)
        .price(new BigDecimal("9.99"))
        .shelfLocation("A1")
        .isActive(true)
        .build();
  }

  public static StoreProduct activeEntity(UUID storeId, UUID brandId, UUID spId) {
    Store store = Store.builder().id(storeId).name("Test Store").type(StoreType.PHYSICAL).build();
    Molecule molecule =
        Molecule.builder()
            .id(UUID.randomUUID())
            .genericName("Ibuprofen")
            .therapeuticClass("NSAID")
            .build();
    Brand brand =
        Brand.builder().id(brandId).molecule(molecule).brandName("Advil").form("Tablet").build();

    return StoreProduct.builder().id(spId).store(store).brand(brand).isActive(true).build();
  }
}
