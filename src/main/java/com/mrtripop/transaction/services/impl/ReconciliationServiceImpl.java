package com.mrtripop.transaction.services.impl;

import com.mrtripop.clinical.models.db.AuditLedger;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.repository.AuditLedgerRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.constant.ErrorCode;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceStatus;
import com.mrtripop.transaction.models.dto.ReconciliationEntryDto;
import com.mrtripop.transaction.models.dto.ReconciliationReportDto;
import com.mrtripop.transaction.models.dto.ReconciliationRequest;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.services.ReconciliationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {

  private static final String ACTION_INVOICE_CREATED = "CREATE";
  private static final String ACTION_INVENTORY_OUT = "INVENTORY_OUT";
  private static final int MONEY_SCALE = 2;

  private final AuditLedgerRepository auditLedgerRepository;
  private final InvoiceRepository invoiceRepository;
  private final StoreRepository storeRepository;

  @Override
  @Transactional(readOnly = true)
  public ReconciliationReportDto generateReport(ReconciliationRequest request) throws ApplicationException {
    Store store = storeRepository.findById(request.getStoreId())
        .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND));

    LocalDateTime periodStartDateTime = Instant.ofEpochMilli(request.getPeriodStart())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    LocalDateTime periodEndDateTime = Instant.ofEpochMilli(request.getPeriodEnd())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();

    // TODO: AuditLedger lacks a storeId column, so ledger queries cannot be scoped per-store.
    // The query below fetches all audit entries globally within the time range. Once a storeId
    // column is added to audit_ledger, filter by store to avoid cross-store contamination.

    List<AuditLedger> ledgerEntries = auditLedgerRepository.findByTimestampBetween(
        periodStartDateTime, periodEndDateTime);

    List<Invoice> completedInvoices = invoiceRepository.findByStoreIdAndStatusAndCreatedAtBetween(
        request.getStoreId(), InvoiceStatus.COMPLETED, request.getPeriodStart(), request.getPeriodEnd());

    BigDecimal ledgerTotal = calculateLedgerTotal(ledgerEntries);
    BigDecimal invoiceTotal = calculateInvoiceTotal(completedInvoices);
    BigDecimal discrepancy = invoiceTotal.subtract(ledgerTotal).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    // TODO: Orphan detection requires storeId on AuditLedger to match INVENTORY_OUT entries
    // against INVOICE_CREATED entries for the same store. Without storeId, cross-entity
    // matching compares StoreStock IDs against Invoice IDs which are unrelated entity types.
    // Enable orphan detection once AuditLedger schema supports per-store scoping.

    List<ReconciliationEntryDto> entries = buildReconciliationEntries(ledgerEntries);

    int discrepancyCount = countDiscrepancies(entries);

    return ReconciliationReportDto.builder()
        .storeId(store.getId())
        .storeName(store.getName())
        .reportDate(request.getReportDate())
        .periodStart(request.getPeriodStart())
        .periodEnd(request.getPeriodEnd())
        .ledgerTotal(ledgerTotal)
        .invoiceTotal(invoiceTotal)
        .discrepancy(discrepancy)
        .discrepancyCount(discrepancyCount)
        .entries(entries)
        .build();
  }

  private BigDecimal calculateLedgerTotal(List<AuditLedger> ledgerEntries) {
    return ledgerEntries.stream()
        .filter(entry -> ACTION_INVOICE_CREATED.equals(entry.getActionType()))
        .map(AuditLedger::getNewValue)
        .filter(value -> value != null && !value.isBlank())
        .map(value -> {
          try {
            return new BigDecimal(value);
          } catch (NumberFormatException e) {
            log.warn("Failed to parse ledger newValue as BigDecimal: {}", value);
            return BigDecimal.ZERO;
          }
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateInvoiceTotal(List<Invoice> completedInvoices) {
    return completedInvoices.stream()
        .map(Invoice::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }

  private List<ReconciliationEntryDto> buildReconciliationEntries(
      List<AuditLedger> ledgerEntries) {
    List<ReconciliationEntryDto> entries = new ArrayList<>();
    for (AuditLedger entry : ledgerEntries) {
      entries.add(ReconciliationEntryDto.builder()
          .actionType(entry.getActionType())
          .entityName(entry.getEntityName())
          .entityId(entry.getEntityId())
          .oldValue(entry.getOldValue())
          .newValue(entry.getNewValue())
          .timestamp(entry.getTimestamp())
          .orphaned(false)
          .build());
    }
    return entries;
  }

  private int countDiscrepancies(List<ReconciliationEntryDto> entries) {
    int count = 0;
    for (ReconciliationEntryDto entry : entries) {
      if (entry.isOrphaned()) {
        count++;
      }
    }
    return count;
  }
}
