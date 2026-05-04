package com.mrtripop.transaction.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.AuditLedger;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.repository.AuditLedgerRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.fixture.ReconciliationFixture;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.dto.ReconciliationReportDto;
import com.mrtripop.transaction.models.dto.ReconciliationRequest;
import com.mrtripop.transaction.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReconciliationServiceImpl")
class ReconciliationServiceImplTest {

  @Mock private AuditLedgerRepository auditLedgerRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private StoreRepository storeRepository;
  @InjectMocks private ReconciliationServiceImpl reconciliationService;

  @Nested
  @DisplayName("GenerateReport")
  class GenerateReport {

    @Test
    @DisplayName("should return report with matching totals when ledger and invoices are consistent")
    void shouldReturnReportWithMatchingTotals() throws ApplicationException {
      // Arrange
      ReconciliationRequest request = ReconciliationFixture.validRequest();
      Store store = ReconciliationFixture.defaultStore();

      AuditLedger ledgerEntry = ReconciliationFixture.invoiceCreatedEntry(
          ReconciliationFixture.ENTITY_ID_INVOICE_1, ReconciliationFixture.LEDGER_AMOUNT);
      Invoice invoice = ReconciliationFixture.completedInvoice(
          ReconciliationFixture.STORE_ID, ReconciliationFixture.INVOICE_TOTAL_AMOUNT);

      when(storeRepository.findById(ReconciliationFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(auditLedgerRepository.findByTimestampBetween(
              any(), any()))
          .thenReturn(List.of(ledgerEntry));
      when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtBetween(
              eq(ReconciliationFixture.STORE_ID), any(), anyLong(), anyLong()))
          .thenReturn(List.of(invoice));

      // Act
      ReconciliationReportDto result = reconciliationService.generateReport(request);

      // Assert
      assertNotNull(result);
      assertEquals(ReconciliationFixture.STORE_ID, result.getStoreId());
      assertEquals(ReconciliationFixture.STORE_NAME, result.getStoreName());
      assertEquals(ReconciliationFixture.REPORT_DATE, result.getReportDate());
      assertEquals(ReconciliationFixture.LEDGER_AMOUNT, result.getLedgerTotal());
      assertEquals(ReconciliationFixture.INVOICE_TOTAL_AMOUNT, result.getInvoiceTotal());
      assertEquals(0, ReconciliationFixture.DISCREPANCY_ZERO.compareTo(result.getDiscrepancy()));
      assertEquals(0, result.getDiscrepancyCount());
      assertEquals(1, result.getEntries().size());
      assertFalse(result.getEntries().get(0).isOrphaned());
    }

    @Test
    @DisplayName("should highlight discrepancy when ledger and invoice totals differ")
    void shouldHighlightDiscrepancy() throws ApplicationException {
      // Arrange
      ReconciliationRequest request = ReconciliationFixture.validRequest();
      Store store = ReconciliationFixture.defaultStore();

      BigDecimal ledgerAmount = new BigDecimal("100.00");
      BigDecimal invoiceAmount = new BigDecimal("150.00");

      AuditLedger ledgerEntry = ReconciliationFixture.invoiceCreatedEntry(
          ReconciliationFixture.ENTITY_ID_INVOICE_1, ledgerAmount);
      Invoice invoice = ReconciliationFixture.completedInvoice(
          ReconciliationFixture.STORE_ID, invoiceAmount);

      when(storeRepository.findById(ReconciliationFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(auditLedgerRepository.findByTimestampBetween(
              any(), any()))
          .thenReturn(List.of(ledgerEntry));
      when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtBetween(
              eq(ReconciliationFixture.STORE_ID), any(), anyLong(), anyLong()))
          .thenReturn(List.of(invoice));

      // Act
      ReconciliationReportDto result = reconciliationService.generateReport(request);

      // Assert
      assertNotNull(result);
      assertEquals(ledgerAmount, result.getLedgerTotal());
      assertEquals(invoiceAmount, result.getInvoiceTotal());
      BigDecimal expectedDiscrepancy = new BigDecimal("50.00");
      assertEquals(0, expectedDiscrepancy.compareTo(result.getDiscrepancy()));
    }

    @Test
    @DisplayName("should mark INVENTORY_OUT entry as orphaned when no matching INVOICE_CREATED exists")
    void shouldMarkOrphanedInventoryOutEntry() throws ApplicationException {
      // Arrange
      ReconciliationRequest request = ReconciliationFixture.validRequest();
      Store store = ReconciliationFixture.defaultStore();

      AuditLedger invoiceCreatedEntry = ReconciliationFixture.invoiceCreatedEntry(
          ReconciliationFixture.ENTITY_ID_INVOICE_1, ReconciliationFixture.LEDGER_AMOUNT);
      AuditLedger orphanedInventoryOut = ReconciliationFixture.inventoryOutEntry(
          ReconciliationFixture.ENTITY_ID_STOCK_MOVE);

      when(storeRepository.findById(ReconciliationFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(auditLedgerRepository.findByTimestampBetween(
              any(), any()))
          .thenReturn(List.of(invoiceCreatedEntry, orphanedInventoryOut));
      when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtBetween(
              eq(ReconciliationFixture.STORE_ID), any(), anyLong(), anyLong()))
          .thenReturn(Collections.emptyList());

      // Act
      ReconciliationReportDto result = reconciliationService.generateReport(request);

      // Assert
      assertNotNull(result);
      assertEquals(2, result.getEntries().size());
      assertFalse(result.getEntries().get(0).isOrphaned());
      assertTrue(result.getEntries().get(1).isOrphaned());
      assertEquals(1, result.getDiscrepancyCount());
    }

    @Test
    @DisplayName("should return empty report with zero totals when no transactions exist for the period")
    void shouldReturnEmptyReportWithZeroTotals() throws ApplicationException {
      // Arrange
      ReconciliationRequest request = ReconciliationFixture.validRequest();
      Store store = ReconciliationFixture.defaultStore();

      when(storeRepository.findById(ReconciliationFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(auditLedgerRepository.findByTimestampBetween(
              any(), any()))
          .thenReturn(Collections.emptyList());
      when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtBetween(
              eq(ReconciliationFixture.STORE_ID), any(), anyLong(), anyLong()))
          .thenReturn(Collections.emptyList());

      // Act
      ReconciliationReportDto result = reconciliationService.generateReport(request);

      // Assert
      assertNotNull(result);
      assertEquals(ReconciliationFixture.STORE_ID, result.getStoreId());
      assertEquals(ReconciliationFixture.STORE_NAME, result.getStoreName());
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getLedgerTotal()));
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getInvoiceTotal()));
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscrepancy()));
      assertEquals(0, result.getDiscrepancyCount());
      assertTrue(result.getEntries().isEmpty());
    }

    @Test
    @DisplayName("should throw STORE_NOT_FOUND when store does not exist")
    void shouldThrowStoreNotFound() {
      // Arrange
      ReconciliationRequest request = ReconciliationFixture.validRequest();
      UUID nonExistentStoreId = UUID.fromString("00000000-0000-0000-0000-999999999999");

      when(storeRepository.findById(nonExistentStoreId)).thenReturn(Optional.empty());

      // Act & Assert
      ReconciliationRequest requestWithInvalidStore = ReconciliationRequest.builder()
          .storeId(nonExistentStoreId)
          .reportDate(ReconciliationFixture.REPORT_DATE)
          .periodStart(ReconciliationFixture.PERIOD_START)
          .periodEnd(ReconciliationFixture.PERIOD_END)
          .build();

      ApplicationException ex = assertThrows(
          ApplicationException.class, () -> reconciliationService.generateReport(requestWithInvalidStore));
      assertEquals(ErrorCode.STORE_NOT_FOUND, ex.getErrorCode());
      verify(auditLedgerRepository, never()).findByTimestampBetween(any(), any());
      verify(invoiceRepository, never()).findByStoreIdAndStatusAndCreatedAtBetween(
          any(), any(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("should sum multiple ledger entries correctly")
    void shouldSumMultipleLedgerEntries() throws ApplicationException {
      // Arrange
      ReconciliationRequest request = ReconciliationFixture.validRequest();
      Store store = ReconciliationFixture.defaultStore();

      BigDecimal amount1 = new BigDecimal("50.00");
      BigDecimal amount2 = new BigDecimal("75.00");
      BigDecimal totalLedger = new BigDecimal("125.00");

      AuditLedger entry1 = ReconciliationFixture.invoiceCreatedEntry(
          ReconciliationFixture.ENTITY_ID_INVOICE_1, amount1);
      AuditLedger entry2 = ReconciliationFixture.invoiceCreatedEntry(
          ReconciliationFixture.ENTITY_ID_INVOICE_2, amount2);

      Invoice invoice = ReconciliationFixture.completedInvoice(
          ReconciliationFixture.STORE_ID, totalLedger);

      when(storeRepository.findById(ReconciliationFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(auditLedgerRepository.findByTimestampBetween(
              any(), any()))
          .thenReturn(List.of(entry1, entry2));
      when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtBetween(
              eq(ReconciliationFixture.STORE_ID), any(), anyLong(), anyLong()))
          .thenReturn(List.of(invoice));

      // Act
      ReconciliationReportDto result = reconciliationService.generateReport(request);

      // Assert
      assertNotNull(result);
      assertEquals(0, totalLedger.compareTo(result.getLedgerTotal()));
      assertEquals(0, totalLedger.compareTo(result.getInvoiceTotal()));
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscrepancy()));
    }

    @Test
    @DisplayName("should not mark INVENTORY_OUT as orphaned when matching INVOICE_CREATED exists")
    void shouldNotMarkInventoryOutAsOrphanedWhenMatchExists() throws ApplicationException {
      // Arrange
      ReconciliationRequest request = ReconciliationFixture.validRequest();
      Store store = ReconciliationFixture.defaultStore();

      String sharedEntityId = ReconciliationFixture.ENTITY_ID_INVOICE_1;

      AuditLedger invoiceCreated = ReconciliationFixture.invoiceCreatedEntry(
          sharedEntityId, ReconciliationFixture.LEDGER_AMOUNT);
      AuditLedger inventoryOut = ReconciliationFixture.inventoryOutEntry(sharedEntityId);

      Invoice invoice = ReconciliationFixture.completedInvoice(
          ReconciliationFixture.STORE_ID, ReconciliationFixture.INVOICE_TOTAL_AMOUNT);

      when(storeRepository.findById(ReconciliationFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(auditLedgerRepository.findByTimestampBetween(
              any(), any()))
          .thenReturn(List.of(invoiceCreated, inventoryOut));
      when(invoiceRepository.findByStoreIdAndStatusAndCreatedAtBetween(
              eq(ReconciliationFixture.STORE_ID), any(), anyLong(), anyLong()))
          .thenReturn(List.of(invoice));

      // Act
      ReconciliationReportDto result = reconciliationService.generateReport(request);

      // Assert
      assertNotNull(result);
      assertEquals(0, result.getDiscrepancyCount());
    }
  }
}
