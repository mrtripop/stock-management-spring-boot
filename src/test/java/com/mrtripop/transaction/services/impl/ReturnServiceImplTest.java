package com.mrtripop.transaction.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.services.BatchService;
import com.mrtripop.transaction.component.ReturnMapper;
import com.mrtripop.transaction.fixture.InvoiceFixture;
import com.mrtripop.transaction.fixture.ReturnFixture;
import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.repository.InvoiceItemRepository;
import com.mrtripop.transaction.repository.InvoiceRepository;
import com.mrtripop.transaction.repository.ReturnItemRepository;
import com.mrtripop.transaction.repository.ReturnRepository;
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
  }
}
