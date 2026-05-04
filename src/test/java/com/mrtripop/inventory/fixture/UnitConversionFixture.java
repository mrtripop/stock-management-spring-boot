package com.mrtripop.inventory.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.inventory.models.db.UnitConversion;
import com.mrtripop.inventory.models.dto.CreateUnitConversionRequest;

import java.util.UUID;

public final class UnitConversionFixture {
    private UnitConversionFixture() {}

    public static CreateUnitConversionRequest validCreateRequest() {
        return CreateUnitConversionRequest.builder()
                .brandId(UUID.randomUUID())
                .fromUnit("BOX")
                .toUnit("TABLET")
                .ratio(30)
                .build();
    }

    public static UnitConversion defaultEntity() {
        Brand brand = Brand.builder()
                .id(UUID.randomUUID())
                .baseUnit("TABLET")
                .build();

        return UnitConversion.builder()
                .id(1L)
                .brand(brand)
                .fromUnit("BOX")
                .toUnit("TABLET")
                .ratio(30)
                .build();
    }

    public static UnitConversion stripToTabletConversion(Brand brand) {
        return UnitConversion.builder()
                .id(2L)
                .brand(brand)
                .fromUnit("STRIP")
                .toUnit("TABLET")
                .ratio(10)
                .build();
    }
}