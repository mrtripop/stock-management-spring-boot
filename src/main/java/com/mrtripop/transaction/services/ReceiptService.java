package com.mrtripop.transaction.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.transaction.models.dto.ReceiptDto;

public interface ReceiptService {

  ReceiptDto generateReceipt(Long invoiceId) throws ApplicationException;
}
