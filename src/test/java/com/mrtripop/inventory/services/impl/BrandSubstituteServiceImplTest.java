package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.fixture.BrandSubstituteFixture;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.models.dto.BrandSubstituteDto;
import com.mrtripop.inventory.repository.StoreStockRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("Suggest substitute brands at the counter")
class BrandSubstituteServiceImplTest {

  @Mock private StoreRepository storeRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private StoreProductRepository storeProductRepository;

  @InjectMocks private BrandSubstituteServiceImpl brandSubstituteService;

  @Nested
  @DisplayName("Pharmacist asks for substitutes with an unknown store or brand")
  class UnknownStoreOrBrand {

    @Test
    @DisplayName("should tell the pharmacist the store was not found when the store does not exist")
    void shouldThrowStoreNotFound_whenStoreDoesNotExist() {
      // Arrange
      when(storeRepository.existsById(BrandSubstituteFixture.STORE_ID)).thenReturn(false);

      // Act
      ApplicationException exception =
          assertThrows(
              ApplicationException.class,
              () ->
                  brandSubstituteService.findSubstitutes(
                      BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID));

      // Assert
      assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
      assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("should tell the pharmacist the brand was not found when the brand does not exist")
    void shouldThrowBrandNotFound_whenBrandDoesNotExist() {
      // Arrange
      when(storeRepository.existsById(BrandSubstituteFixture.STORE_ID)).thenReturn(true);
      when(brandRepository.findById(BrandSubstituteFixture.REQUESTED_BRAND_ID))
          .thenReturn(Optional.empty());

      // Act
      ApplicationException exception =
          assertThrows(
              ApplicationException.class,
              () ->
                  brandSubstituteService.findSubstitutes(
                      BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID));

      // Assert
      assertEquals(ErrorCode.BRAND_NOT_FOUND, exception.getErrorCode());
      assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }
  }

  @Nested
  @DisplayName("Substitute must be clinically equivalent to the requested brand")
  class ClinicalEquivalence {

    @Test
    @DisplayName("should suggest nothing when the requested brand has no strength recorded")
    void shouldReturnEmpty_whenRequestedBrandHasNoStrength() throws ApplicationException {
      // Arrange
      givenStoreAndBrand(BrandSubstituteFixture.requestedBrandWithoutStrength());

      // Act
      List<BrandSubstituteDto> result =
          brandSubstituteService.findSubstitutes(
              BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID);

      // Assert
      assertTrue(result.isEmpty());
      verify(storeStockRepository, never())
          .findAvailableSubstituteStock(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should suggest nothing when the requested brand has no form recorded")
    void shouldReturnEmpty_whenRequestedBrandHasNoForm() throws ApplicationException {
      // Arrange
      givenStoreAndBrand(BrandSubstituteFixture.requestedBrandWithoutForm());

      // Act
      List<BrandSubstituteDto> result =
          brandSubstituteService.findSubstitutes(
              BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID);

      // Assert
      assertTrue(result.isEmpty());
      verify(storeStockRepository, never())
          .findAvailableSubstituteStock(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should suggest nothing when no equivalent brand is in stock")
    void shouldReturnEmpty_whenNoEquivalentBrandInStock() throws ApplicationException {
      // Arrange
      givenStoreAndBrand(BrandSubstituteFixture.requestedBrand());
      givenEquivalentStock(Collections.emptyList());

      // Act
      List<BrandSubstituteDto> result =
          brandSubstituteService.findSubstitutes(
              BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID);

      // Assert
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("Pharmacist sees in-stock substitutes they can sell")
  class SellableSubstitutes {

    @Test
    @DisplayName(
        "should suggest an equivalent brand with its price, total stock and nearest expiry batch")
    void shouldSuggestEquivalentBrand_withPriceStockAndNearestBatch() throws ApplicationException {
      // Arrange
      Brand panadol = BrandSubstituteFixture.panadolBrand();
      LocalDate nearExpiryDate = BrandSubstituteFixture.nearExpiryDate();
      givenStoreAndBrand(BrandSubstituteFixture.requestedBrand());
      givenEquivalentStock(
          List.of(
              BrandSubstituteFixture.stock(
                  panadol,
                  BrandSubstituteFixture.PANADOL_NEAR_BATCH_ID,
                  nearExpiryDate,
                  BrandSubstituteFixture.PANADOL_NEAR_QUANTITY),
              BrandSubstituteFixture.stock(
                  panadol,
                  BrandSubstituteFixture.PANADOL_FAR_BATCH_ID,
                  BrandSubstituteFixture.farExpiryDate(),
                  BrandSubstituteFixture.PANADOL_FAR_QUANTITY)));
      when(storeProductRepository.findByStoreIdAndBrandIdInAndIsActiveTrueAndPriceIsNotNull(
              BrandSubstituteFixture.STORE_ID, Set.of(BrandSubstituteFixture.PANADOL_BRAND_ID)))
          .thenReturn(
              List.of(
                  BrandSubstituteFixture.storeProduct(
                      panadol, BrandSubstituteFixture.PANADOL_PRICE)));

      // Act
      List<BrandSubstituteDto> result =
          brandSubstituteService.findSubstitutes(
              BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID);

      // Assert
      assertEquals(1, result.size());
      BrandSubstituteDto substitute = result.get(0);
      assertEquals(BrandSubstituteFixture.PANADOL_BRAND_ID, substitute.getBrandId());
      assertEquals(BrandSubstituteFixture.PANADOL_BRAND_NAME, substitute.getBrandName());
      assertEquals(BrandSubstituteFixture.STRENGTH, substitute.getStrength());
      assertEquals(BrandSubstituteFixture.FORM, substitute.getForm());
      assertEquals(BrandSubstituteFixture.PANADOL_PRICE, substitute.getPrice());
      assertEquals(
          BrandSubstituteFixture.PANADOL_TOTAL_QUANTITY, substitute.getAvailableQuantity());
      assertEquals(nearExpiryDate, substitute.getNearestExpiryDate());
      assertEquals(
          BrandSubstituteFixture.PANADOL_NEAR_BATCH_ID, substitute.getNearestExpiryBatchId());
    }

    @Test
    @DisplayName("should leave out an in-stock brand the store has not activated with a price")
    void shouldExcludeBrand_whenNotActivatedWithPrice() throws ApplicationException {
      // Arrange
      Brand panadol = BrandSubstituteFixture.panadolBrand();
      Brand calpol = BrandSubstituteFixture.calpolBrand();
      givenStoreAndBrand(BrandSubstituteFixture.requestedBrand());
      givenEquivalentStock(
          List.of(
              BrandSubstituteFixture.stock(
                  panadol,
                  BrandSubstituteFixture.PANADOL_NEAR_BATCH_ID,
                  BrandSubstituteFixture.nearExpiryDate(),
                  BrandSubstituteFixture.PANADOL_NEAR_QUANTITY),
              BrandSubstituteFixture.stock(
                  calpol,
                  BrandSubstituteFixture.CALPOL_BATCH_ID,
                  BrandSubstituteFixture.midExpiryDate(),
                  BrandSubstituteFixture.CALPOL_QUANTITY)));
      when(storeProductRepository.findByStoreIdAndBrandIdInAndIsActiveTrueAndPriceIsNotNull(
              BrandSubstituteFixture.STORE_ID,
              Set.of(
                  BrandSubstituteFixture.PANADOL_BRAND_ID, BrandSubstituteFixture.CALPOL_BRAND_ID)))
          .thenReturn(
              List.of(
                  BrandSubstituteFixture.storeProduct(
                      calpol, BrandSubstituteFixture.CALPOL_PRICE)));

      // Act
      List<BrandSubstituteDto> result =
          brandSubstituteService.findSubstitutes(
              BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID);

      // Assert
      assertEquals(1, result.size());
      assertEquals(BrandSubstituteFixture.CALPOL_BRAND_ID, result.get(0).getBrandId());
      assertEquals(BrandSubstituteFixture.CALPOL_PRICE, result.get(0).getPrice());
    }

    @Test
    @DisplayName("should list the brand whose stock expires soonest first")
    void shouldOrderBrandsByNearestExpiry() throws ApplicationException {
      // Arrange
      Brand panadol = BrandSubstituteFixture.panadolBrand();
      Brand calpol = BrandSubstituteFixture.calpolBrand();
      givenStoreAndBrand(BrandSubstituteFixture.requestedBrand());
      givenEquivalentStock(
          List.of(
              BrandSubstituteFixture.stock(
                  calpol,
                  BrandSubstituteFixture.CALPOL_BATCH_ID,
                  BrandSubstituteFixture.nearExpiryDate(),
                  BrandSubstituteFixture.CALPOL_QUANTITY),
              BrandSubstituteFixture.stock(
                  panadol,
                  BrandSubstituteFixture.PANADOL_NEAR_BATCH_ID,
                  BrandSubstituteFixture.midExpiryDate(),
                  BrandSubstituteFixture.PANADOL_NEAR_QUANTITY)));
      when(storeProductRepository.findByStoreIdAndBrandIdInAndIsActiveTrueAndPriceIsNotNull(
              BrandSubstituteFixture.STORE_ID,
              Set.of(
                  BrandSubstituteFixture.PANADOL_BRAND_ID, BrandSubstituteFixture.CALPOL_BRAND_ID)))
          .thenReturn(
              List.of(
                  BrandSubstituteFixture.storeProduct(
                      panadol, BrandSubstituteFixture.PANADOL_PRICE),
                  BrandSubstituteFixture.storeProduct(
                      calpol, BrandSubstituteFixture.CALPOL_PRICE)));

      // Act
      List<BrandSubstituteDto> result =
          brandSubstituteService.findSubstitutes(
              BrandSubstituteFixture.STORE_ID, BrandSubstituteFixture.REQUESTED_BRAND_ID);

      // Assert
      assertEquals(
          List.of(BrandSubstituteFixture.CALPOL_BRAND_ID, BrandSubstituteFixture.PANADOL_BRAND_ID),
          result.stream().map(BrandSubstituteDto::getBrandId).toList());
    }
  }

  private void givenStoreAndBrand(Brand requestedBrand) {
    when(storeRepository.existsById(BrandSubstituteFixture.STORE_ID)).thenReturn(true);
    when(brandRepository.findById(BrandSubstituteFixture.REQUESTED_BRAND_ID))
        .thenReturn(Optional.of(requestedBrand));
  }

  private void givenEquivalentStock(List<StoreStock> stocks) {
    when(storeStockRepository.findAvailableSubstituteStock(
            BrandSubstituteFixture.STORE_ID,
            BrandSubstituteFixture.MOLECULE_ID,
            BrandSubstituteFixture.STRENGTH,
            BrandSubstituteFixture.FORM,
            BrandSubstituteFixture.REQUESTED_BRAND_ID))
        .thenReturn(stocks);
  }
}
