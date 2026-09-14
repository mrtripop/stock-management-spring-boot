package com.mrtripop.transaction.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.transaction.component.ReturnMapper;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.fixture.InvoiceFixture;
import com.mrtripop.transaction.fixture.ReturnFixture;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.models.dto.ReturnItemRequest;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.repository.ReturnItemRepository;
import com.mrtripop.transaction.repository.ReturnRepository;
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
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReturnServiceImpl")
class ReturnServiceImplTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceItemRepository invoiceItemRepository;
  @Mock private ReturnRepository returnRepository;
  @Mock private ReturnItemRepository returnItemRepository;
  @Mock private ReturnMapper returnMapper;
  @Mock private AuditService auditService;
  @Mock private BatchService batchService;
  @InjectMocks private ReturnServiceImpl returnService;

  @Nested
  @DisplayName("CreateReturn")
  class CreateReturn {

    @Test
    @DisplayName("should restock and refund a partial quantity of one invoice line")
    void shouldCreateReturnForSingleLine() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());
      Return savedReturn = ReturnFixture.validReturn(invoice);
      ReturnDto dto = ReturnDto.builder().id(1L).invoiceId(invoice.getId()).build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(auditService.recordAudit(anyString(), anyString(), anyString(), anyString(), anyString()))
          .thenReturn(null);
      when(returnMapper.toDto(any(Return.class))).thenReturn(dto);
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      ReturnDto result = returnService.createReturn(1L, request);

      // Assert
      assertNotNull(result);
      verify(batchService).restoreStock(
          InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, ReturnFixture.RETURN_QUANTITY);
      verify(returnRepository).save(argThat(r ->
          r.getTotalRefundAmount().compareTo(ReturnFixture.EXPECTED_REFUND_AMOUNT) == 0));
      verify(invoiceRepository).save(argThat(inv ->
          inv.getTotalAmount().compareTo(ReturnFixture.EXPECTED_NEW_TOTAL_AMOUNT) == 0
              && inv.getPatientOwed().compareTo(ReturnFixture.EXPECTED_NEW_PATIENT_OWED) == 0
              && inv.getInsuranceClaimAmount()
                  .compareTo(ReturnFixture.EXPECTED_NEW_INSURANCE_CLAIM) == 0));
      verify(auditService).recordAudit(
          eq("RETURN"), eq("Invoice"), eq("1"), anyString(), anyString());
    }

    @Test
    @DisplayName("should throw TXN4012 when invoice is still pending")
    void shouldThrowNotCompletedWhenPending() {
      // Arrange
      Invoice invoice = InvoiceFixture.pendingInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_NOT_COMPLETED, ex.getErrorCode());
      verify(returnRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw TXN4012 when invoice is voided")
    void shouldThrowNotCompletedWhenVoided() {
      // Arrange
      Invoice invoice = InvoiceFixture.voidedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_NOT_COMPLETED, ex.getErrorCode());
      verify(returnRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw TXN4001 when invoice not found")
    void shouldThrowInvoiceNotFound() {
      // Arrange
      CreateReturnRequest request = ReturnFixture.validCreateRequest(1L);
      when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(com.mrtripop.transaction.constant.ErrorCode.INVOICE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw TXN4014 when invoice item does not exist")
    void shouldThrowInvoiceItemNotFound() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      CreateReturnRequest request = ReturnFixture.validCreateRequest(999L);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_ITEM_NOT_FOUND, ex.getErrorCode());
      verify(batchService, never()).restoreStock(any(), any(), any());
    }

    @Test
    @DisplayName("should throw TXN4014 when invoice item belongs to a different invoice")
    void shouldThrowInvoiceItemNotFoundForMismatchedInvoice() {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      Invoice otherInvoice = InvoiceFixture.completedInvoice();
      otherInvoice.setId(2L);
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(otherInvoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.INVOICE_ITEM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should throw TXN4013 when return quantity exceeds remaining quantity")
    void shouldThrowExceedsAvailableWhenOverQuantity() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      ReturnItemRequest overRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(InvoiceFixture.VALID_QUANTITY + 1)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(overRequest))
          .build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.RETURN_QUANTITY_EXCEEDS_AVAILABLE, ex.getErrorCode());
      verify(batchService, never()).restoreStock(any(), any(), any());
    }

    @Test
    @DisplayName("should account for a prior return when checking remaining quantity")
    void shouldRespectAlreadyReturnedQuantity() throws ApplicationException {
      // Arrange — 5 total, 2 already returned, requesting exactly the 3 remaining
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      ReturnItemRequest boundaryRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(3L)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(boundaryRequest))
          .build();
      Return savedReturn = ReturnFixture.validReturn(invoice);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(2L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(returnMapper.toDto(any(Return.class)))
          .thenReturn(ReturnDto.builder().id(1L).build());
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      ReturnDto result = returnService.createReturn(1L, request);

      // Assert
      assertNotNull(result);
      verify(batchService).restoreStock(InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, 3L);
    }

    @Test
    @DisplayName("should reject a second return once the prior return already covers the item")
    void shouldRejectWhenAlreadyReturnedExceedsRequest() {
      // Arrange — 5 total, 2 already returned, requesting 4 (only 3 remain)
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      ReturnItemRequest tooMuchRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(4L)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(tooMuchRequest))
          .build();

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(2L);

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(ErrorCode.RETURN_QUANTITY_EXCEEDS_AVAILABLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("should restock and refund two lines in a single request")
    void shouldCreateReturnForMultipleLines() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem firstItem = InvoiceFixture.validInvoiceItem(invoice);
      InvoiceItem secondItem = ReturnFixture.invoiceItemForRounding(invoice);
      ReturnItemRequest firstRequest = ReturnItemRequest.builder()
          .invoiceItemId(firstItem.getId())
          .quantity(ReturnFixture.RETURN_QUANTITY)
          .build();
      ReturnItemRequest secondRequest = ReturnItemRequest.builder()
          .invoiceItemId(secondItem.getId())
          .quantity(ReturnFixture.ROUNDING_RETURN_QUANTITY)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(firstRequest, secondRequest))
          .build();
      Return savedReturn = ReturnFixture.validReturn(invoice);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(firstItem.getId())).thenReturn(Optional.of(firstItem));
      when(invoiceItemRepository.findById(secondItem.getId())).thenReturn(Optional.of(secondItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(any())).thenReturn(0L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(returnMapper.toDto(any(Return.class)))
          .thenReturn(ReturnDto.builder().id(1L).build());
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // Act
      ReturnDto result = returnService.createReturn(1L, request);

      // Assert
      assertNotNull(result);
      verify(batchService).restoreStock(
          InvoiceFixture.STORE_ID, InvoiceFixture.BATCH_ID, ReturnFixture.RETURN_QUANTITY);
      verify(returnRepository).save(argThat(r -> r.getTotalRefundAmount().compareTo(
          ReturnFixture.EXPECTED_REFUND_AMOUNT.add(ReturnFixture.EXPECTED_ROUNDED_REFUND_AMOUNT)) == 0));
    }

    @Test
    @DisplayName("should round refund split to the nearest cent using HALF_UP")
    void shouldRoundRefundSplitCorrectly() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = ReturnFixture.invoiceItemForRounding(invoice);
      ReturnItemRequest itemRequest = ReturnItemRequest.builder()
          .invoiceItemId(invoiceItem.getId())
          .quantity(ReturnFixture.ROUNDING_RETURN_QUANTITY)
          .build();
      CreateReturnRequest request = CreateReturnRequest.builder()
          .reason(com.mrtripop.transaction.models.db.ReturnReason.OTHER)
          .items(List.of(itemRequest))
          .build();
      Return savedReturn = ReturnFixture.validReturn(invoice);

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);
      when(returnRepository.save(any(Return.class))).thenReturn(savedReturn);
      when(returnItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
      when(returnMapper.toDto(any(Return.class)))
          .thenReturn(ReturnDto.builder().id(1L).build());
      when(returnMapper.toItemDtoList(anyList())).thenReturn(List.of());

      // createReturn mutates this same Invoice instance in place, so the original
      // amounts must be captured before Act — reading them afterward would read
      // the already-decremented values and double-subtract the refund.
      BigDecimal originalTotalAmount = invoice.getTotalAmount();
      BigDecimal originalPatientOwed = invoice.getPatientOwed();
      BigDecimal originalInsuranceClaim = invoice.getInsuranceClaimAmount();

      // Act
      returnService.createReturn(1L, request);

      // Assert
      verify(invoiceRepository).save(argThat(inv -> {
        BigDecimal expectedTotal = originalTotalAmount
            .subtract(ReturnFixture.EXPECTED_ROUNDED_REFUND_AMOUNT);
        BigDecimal expectedPatientOwed = originalPatientOwed
            .subtract(ReturnFixture.EXPECTED_ROUNDED_PATIENT_OWED);
        BigDecimal expectedInsurance = originalInsuranceClaim
            .subtract(ReturnFixture.EXPECTED_ROUNDED_INSURANCE_CLAIM);
        return inv.getTotalAmount().compareTo(expectedTotal) == 0
            && inv.getPatientOwed().compareTo(expectedPatientOwed) == 0
            && inv.getInsuranceClaimAmount().compareTo(expectedInsurance) == 0;
      }));
    }

    @Test
    @DisplayName("should roll back and record nothing when restock fails")
    void shouldRollbackWhenRestoreStockFails() throws ApplicationException {
      // Arrange
      Invoice invoice = InvoiceFixture.completedInvoice();
      InvoiceItem invoiceItem = InvoiceFixture.validInvoiceItem(invoice);
      CreateReturnRequest request = ReturnFixture.validCreateRequest(invoiceItem.getId());

      when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
      when(invoiceItemRepository.findById(invoiceItem.getId())).thenReturn(Optional.of(invoiceItem));
      when(returnItemRepository.sumQuantityByInvoiceItemId(invoiceItem.getId())).thenReturn(0L);
      doThrow(new ApplicationException(
              com.mrtripop.inventory.constant.ErrorCode.STOCK_NOT_FOUND_FOR_BATCH,
              HttpStatus.NOT_FOUND))
          .when(batchService).restoreStock(any(), any(), any());

      // Act & Assert
      ApplicationException ex =
          assertThrows(ApplicationException.class, () -> returnService.createReturn(1L, request));
      assertEquals(com.mrtripop.inventory.constant.ErrorCode.STOCK_NOT_FOUND_FOR_BATCH,
          ex.getErrorCode());
      verify(returnRepository, never()).save(any());
      verify(invoiceRepository, never()).save(any());
    }
  }
}
