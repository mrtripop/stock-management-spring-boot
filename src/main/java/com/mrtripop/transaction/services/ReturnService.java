package com.mrtripop.transaction.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.models.dto.CreateReturnRequest;
import com.mrtripop.transaction.models.dto.ReturnDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReturnService {

  ReturnDto createReturn(Long invoiceId, CreateReturnRequest request) throws ApplicationException;

  ReturnDto findById(Long id) throws ApplicationException;

  Page<ReturnDto> findByInvoiceId(Long invoiceId, Pageable pageable) throws ApplicationException;
}
