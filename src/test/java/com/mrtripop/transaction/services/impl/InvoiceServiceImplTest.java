package com.mrtripop.transaction.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.StoreStock;
import com.mrtripop.inventory.repository.BatchRepository;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.transaction.component.InvoiceMapper;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.fixture.InvoiceFixture;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.dto.CreateInvoiceRequest;
import com.mrtripop.transaction.models.dto.InvoiceDto;
import com.mrtripop.transaction.models.dto.InvoiceItemDto;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("InvoiceServiceImpl")
class InvoiceServiceImplTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceItemRepository invoiceItemRepository;
  @Mock private StoreRepository storeRepository;
  @Mock private BrandRepository brandRepository;
  @Mock private BatchRepository batchRepository;
  @Mock private StoreProductRepository storeProductRepository;
  @Mock private StoreStockRepository storeStockRepository;
  @Mock private InvoiceMapper invoiceMapper;
  @Mock private AuditService auditService;
  @InjectMocks private InvoiceServiceImpl invoiceService;

  @Nested
  @DisplayName("CreateInvoice")
  class CreateInvoice {

    @Test
    @DisplayName("should create invoice with split-pay items")
    void shouldCreateInvoiceWithSplitPay() throws ApplicationException {
      // Arrange
      CreateInvoiceRequest request = InvoiceFixture.validCreateRequest();
      Store store = InvoiceFixture.validStore();
      Batch batch = InvoiceFixture.validBatch();
      StoreProduct storeProduct = InvoiceFixture.validStoreProduct();
      StoreStock storeStock = InvoiceFixture.validStoreStock();
      Invoice savedInvoice = InvoiceFixture.pendingInvoice();
      InvoiceItem savedItem = InvoiceFixture.validInvoiceItem(savedInvoice);
      InvoiceDto dto = InvoiceDto.builder()
          .id(1L)
          .storeId(InvoiceFixture.STORE_ID)
          .storeName(InvoiceFixture.STORE_NAME)
          .build();
      InvoiceItemDto itemDto = InvoiceItemDto.builder()
          .id(1L)
          .brandName(InvoiceFixture.BRAND_NAME)
          .build();

      when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(brandRepository.findById(InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(InvoiceFixture.validBrand()));
      when(batchRepository.findById(InvoiceFixture.BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.findByStoreIdAndBatchId(InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID))
          .thenReturn(Optional.of(storeStock));
      when(storeProductRepository.findByStoreIdAndBrandId(InvoiceFixture.STORE_ID, InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(storeProduct));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(iom -> {
        Invoice entity = iom.getArgument(0);
        if (entity.getId() == null) {
          entity.setId(1L);
        }
        return entity;
      });
      when(invoiceItemRepository.saveAll(anyList())).thenReturn(List.of(savedItem));
      when(auditService.recordAudit(anyString(), anyString(), anyString(), any(), anyString()))
          .thenReturn(null);
      when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
      when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of(itemDto));

      // Act
      InvoiceDto result = invoiceService.create(request);

      // Assert
      assertNotNull(result);
      verify(invoiceRepository).save(any(Invoice.class));
      verify(invoiceItemRepository).saveAll(anyList());
      verify(auditService).recordAudit(eq("CREATE"), eq("Invoice"), anyString(), isNull(),
          anyString());
    }

    @Test
    @DisplayName("should create invoice with no insurance coverage (0%)")
    void shouldCreateInvoiceWithNoInsurance() throws ApplicationException {
      // Arrange
      CreateInvoiceRequest request = InvoiceFixture.createRequestNoInsurance();
      Store store = InvoiceFixture.validStore();
      Batch batch = InvoiceFixture.validBatch();
      StoreProduct storeProduct = InvoiceFixture.validStoreProduct();
      StoreStock storeStock = InvoiceFixture.validStoreStock();
      InvoiceDto dto = InvoiceDto.builder()
          .id(1L)
          .storeId(InvoiceFixture.STORE_ID)
          .storeName(InvoiceFixture.STORE_NAME)
          .build();
      InvoiceItemDto itemDto = InvoiceItemDto.builder()
          .id(1L)
          .brandName(InvoiceFixture.BRAND_NAME)
          .build();

      when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(brandRepository.findById(InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(InvoiceFixture.validBrand()));
      when(batchRepository.findById(InvoiceFixture.BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.findByStoreIdAndBatchId(InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID))
          .thenReturn(Optional.of(storeStock));
      when(storeProductRepository.findByStoreIdAndBrandId(InvoiceFixture.STORE_ID, InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(storeProduct));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(iom -> {
        Invoice entity = iom.getArgument(0);
        if (entity.getId() == null) {
          entity.setId(1L);
        }
        return entity;
      });
      when(invoiceItemRepository.saveAll(anyList())).thenReturn(List.of());
      when(auditService.recordAudit(anyString(), anyString(), anyString(), any(), anyString()))
          .thenReturn(null);
      when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
      when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of(itemDto));

      // Act
      InvoiceDto result = invoiceService.create(request);

      // Assert
      assertNotNull(result);
      verify(invoiceRepository).save(argThat(inv -> inv.getPatientOwed().compareTo(
          inv.getTotalAmount()) == 0));
    }

    @Test
    @DisplayName("should create invoice with 100% insurance coverage")
    void shouldCreateInvoiceWithFullInsurance() throws ApplicationException {
      // Arrange
      CreateInvoiceRequest request = InvoiceFixture.createRequestFullInsurance();
      Store store = InvoiceFixture.validStore();
      Batch batch = InvoiceFixture.validBatch();
      StoreProduct storeProduct = InvoiceFixture.validStoreProduct();
      StoreStock storeStock = InvoiceFixture.validStoreStock();
      InvoiceDto dto = InvoiceDto.builder()
          .id(1L)
          .storeId(InvoiceFixture.STORE_ID)
          .storeName(InvoiceFixture.STORE_NAME)
          .build();
      InvoiceItemDto itemDto = InvoiceItemDto.builder()
          .id(1L)
          .brandName(InvoiceFixture.BRAND_NAME)
          .build();

      when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(brandRepository.findById(InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(InvoiceFixture.validBrand()));
      when(batchRepository.findById(InvoiceFixture.BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.findByStoreIdAndBatchId(InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID))
          .thenReturn(Optional.of(storeStock));
      when(storeProductRepository.findByStoreIdAndBrandId(InvoiceFixture.STORE_ID, InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(storeProduct));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(iom -> {
        Invoice entity = iom.getArgument(0);
        if (entity.getId() == null) {
          entity.setId(1L);
        }
        return entity;
      });
      when(invoiceItemRepository.saveAll(anyList())).thenReturn(List.of());
      when(auditService.recordAudit(anyString(), anyString(), anyString(), any(), anyString()))
          .thenReturn(null);
      when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
      when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of(itemDto));

      // Act
      InvoiceDto result = invoiceService.create(request);

      // Assert
      assertNotNull(result);
      verify(invoiceRepository).save(argThat(inv -> inv.getPatientOwed().compareTo(
          BigDecimal.ZERO) == 0));
    }

    @Test
    @DisplayName("should throw TXN4006 when store not found")
    void shouldThrowStoreNotFound() {
      // Arrange
      CreateInvoiceRequest request = InvoiceFixture.validCreateRequest();
      when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.create(request));
      assertEquals(ErrorCode.STORE_NOT_FOUND, ex.getErrorCode());
      verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw TXN4007 when brand not found")
    void shouldThrowBrandNotFound() {
      // Arrange
      CreateInvoiceRequest request = InvoiceFixture.validCreateRequest();
      Store store = InvoiceFixture.validStore();
      when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(brandRepository.findById(InvoiceFixture.BRAND_ID)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.create(request));
      assertEquals(ErrorCode.BRAND_NOT_FOUND, ex.getErrorCode());
      verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw TXN4008 when batch not found")
    void shouldThrowBatchNotFound() {
      // Arrange
      CreateInvoiceRequest request = InvoiceFixture.validCreateRequest();
      Store store = InvoiceFixture.validStore();
      when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(brandRepository.findById(InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(InvoiceFixture.validBrand()));
      when(batchRepository.findById(InvoiceFixture.BATCH_ID)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.create(request));
      assertEquals(ErrorCode.BATCH_NOT_FOUND, ex.getErrorCode());
      verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw TXN4003 when insufficient stock")
    void shouldThrowInsufficientStock() {
      // Arrange
      CreateInvoiceRequest request = InvoiceFixture.validCreateRequest();
      Store store = InvoiceFixture.validStore();
      Batch batch = InvoiceFixture.validBatch();
      StoreStock lowStock = StoreStock.builder()
          .id(1L)
          .store(store)
          .batch(batch)
          .quantity(1L)
          .build();

      when(storeRepository.findById(InvoiceFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(brandRepository.findById(InvoiceFixture.BRAND_ID))
          .thenReturn(Optional.of(InvoiceFixture.validBrand()));
      when(batchRepository.findById(InvoiceFixture.BATCH_ID)).thenReturn(Optional.of(batch));
      when(storeStockRepository.findByStoreIdAndBatchId(InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID))
          .thenReturn(Optional.of(lowStock));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.create(request));
      assertEquals(ErrorCode.INSUFFICIENT_STOCK, ex.getErrorCode());
      verify(invoiceRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("CompleteInvoice")
  class CompleteInvoice {

    @Test
    @DisplayName("should complete a pending invoice")
    void shouldCompletePendingInvoice() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.pendingInvoice();
      InvoiceDto dto = InvoiceDto.builder()
          .id(1L)
          .storeId(InvoiceFixture.STORE_ID)
          .storeName(InvoiceFixture.STORE_NAME)
          .build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
          .thenReturn(null);
      when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
      when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());
      when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      InvoiceDto result = invoiceService.complete(1L);

      // Assert
      assertNotNull(result);
      verify(invoiceRepository).save(argThat(inv -> inv.getStatus()
          == com.mrtripop.transaction.models.db.InvoiceStatus.COMPLETED));
    }

    @Test
    @DisplayName("should throw TXN4001 when invoice not found")
    void shouldThrowNotFound() {
      // Arrange
      when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.complete(1L));
      assertEquals(ErrorCode.INVOICE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw TXN4004 when invoice already completed")
    void shouldThrowAlreadyCompleted() {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.complete(1L));
      assertEquals(ErrorCode.INVOICE_ALREADY_COMPLETED, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw TXN4005 when invoice already voided")
    void shouldThrowAlreadyVoided() {
      // Arrange
      Invoice invoice = InvoiceFixture.voidedInvoice();
      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.complete(1L));
      assertEquals(ErrorCode.INVOICE_ALREADY_VOIDED, ex.getErrorCode());
    }
  }

  @Nested
  @DisplayName("VoidInvoice")
  class VoidInvoice {

    @Test
    @DisplayName("should void a pending invoice")
    void shouldVoidPendingInvoice() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.pendingInvoice();
      InvoiceDto dto = InvoiceDto.builder()
          .id(1L)
          .storeId(InvoiceFixture.STORE_ID)
          .storeName(InvoiceFixture.STORE_NAME)
          .build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
          .thenReturn(null);
      when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(dto);
      when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());
      when(invoiceMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      InvoiceDto result = invoiceService.voidInvoice(1L);

      // Assert
      assertNotNull(result);
      verify(invoiceRepository).save(argThat(inv -> inv.getStatus()
          == com.mrtripop.transaction.models.db.InvoiceStatus.VOIDED));
    }

    @Test
    @DisplayName("should throw TXN4001 when invoice not found")
    void shouldThrowNotFound() {
      // Arrange
      when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.voidInvoice(1L));
      assertEquals(ErrorCode.INVOICE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw TXN4005 when invoice already voided")
    void shouldThrowAlreadyVoided() {
      // Arrange
      Invoice invoice = InvoiceFixture.voidedInvoice();
      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.voidInvoice(1L));
      assertEquals(ErrorCode.INVOICE_ALREADY_VOIDED, ex.getErrorCode());
    }
  }

  @Nested
  @DisplayName("FindInvoice")
  class FindInvoice {

    @Test
    @DisplayName("should return invoice by ID with items")
    void shouldReturnInvoiceById() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.pendingInvoice();
      InvoiceItem item = InvoiceFixture.validInvoiceItem(invoice);
      InvoiceDto dto = InvoiceDto.builder()
          .id(1L)
          .storeId(InvoiceFixture.STORE_ID)
          .storeName(InvoiceFixture.STORE_NAME)
          .build();
      InvoiceItemDto itemDto = InvoiceItemDto.builder()
          .id(1L)
          .brandName(InvoiceFixture.BRAND_NAME)
          .build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of(item));
      when(invoiceMapper.toDto(invoice)).thenReturn(dto);
      when(invoiceMapper.toItemDtoList(List.of(item))).thenReturn(List.of(itemDto));

      // Act
      InvoiceDto result = invoiceService.findById(1L);

      // Assert
      assertNotNull(result);
      assertNotNull(result.getItems());
      assertEquals(1, result.getItems().size());
    }

    @Test
    @DisplayName("should throw TXN4001 when invoice not found")
    void shouldThrowNotFound() {
      // Arrange
      when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> invoiceService.findById(1L));
      assertEquals(ErrorCode.INVOICE_NOT_FOUND, ex.getErrorCode());
    }
  }

  @Nested
  @DisplayName("FindAllInvoices")
  class FindAllInvoices {

    @Test
    @DisplayName("should return paginated list of invoices")
    void shouldReturnPaginatedList() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.pendingInvoice();
      Page<Invoice> page = new PageImpl<>(List.of(invoice));
      InvoiceDto dto = InvoiceDto.builder()
          .id(1L)
          .storeId(InvoiceFixture.STORE_ID)
          .storeName(InvoiceFixture.STORE_NAME)
          .build();

      when(invoiceRepository.findByStoreId(eq(InvoiceFixture.STORE_ID), any(Pageable.class)))
          .thenReturn(page);
      when(invoiceMapper.toDto(invoice)).thenReturn(dto);

      // Act
      Page<InvoiceDto> result = invoiceService.findAll(InvoiceFixture.STORE_ID, Pageable.unpaged());

      // Assert
      assertEquals(1, result.getTotalElements());
      assertEquals(InvoiceFixture.STORE_NAME, result.getContent().get(0).getStoreName());
    }
  }
}
