package com.mrtripop.transaction.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.models.dto.CreateInvoiceRequest;
import com.mrtripop.transaction.models.dto.DailySalesSummaryDto;
import com.mrtripop.transaction.models.dto.InvoiceDto;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {

  Page<InvoiceDto> findAll(UUID storeId, Pageable pageable) throws ApplicationException;

  InvoiceDto findById(Long id) throws ApplicationException;

  InvoiceDto create(CreateInvoiceRequest request) throws ApplicationException;

  InvoiceDto complete(Long id) throws ApplicationException;

  InvoiceDto voidInvoice(Long id) throws ApplicationException;

  InvoiceDto dispense(CreateInvoiceRequest request) throws ApplicationException;

  DailySalesSummaryDto getDailySummary(UUID storeId, LocalDate date) throws ApplicationException;
}
