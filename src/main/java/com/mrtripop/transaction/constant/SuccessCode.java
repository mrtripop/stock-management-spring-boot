package com.mrtripop.transaction.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseStatusCode {
  TXN2001_GET_ALL_INVOICES_IS_SUCCESS("TXN2001", "Get all invoices is success"),
  TXN2002_GET_INVOICE_BY_ID_IS_SUCCESS("TXN2002", "Get invoice by ID is success"),
  TXN2003_CREATE_INVOICE_IS_SUCCESS("TXN2003", "Create invoice is success"),
  TXN2004_COMPLETE_INVOICE_IS_SUCCESS("TXN2004", "Complete invoice is success"),
  TXN2005_VOID_INVOICE_IS_SUCCESS("TXN2005", "Void invoice is success"),
  TXN2006_GENERATE_RECEIPT_IS_SUCCESS("TXN2006", "Generate receipt is success"),
  TXN2007_GENERATE_RECONCILIATION_REPORT_IS_SUCCESS(
      "TXN2007", "Generate reconciliation report is success"),
  TXN2008_DISPENSE_IS_SUCCESS("TXN2008", "Dispense is success"),
  TXN2009_GET_DAILY_SUMMARY_IS_SUCCESS("TXN2009", "Get daily summary is success"),
  TXN2010_CREATE_RETURN_IS_SUCCESS("TXN2010", "Create return is success"),
  TXN2011_GET_RETURNS_BY_INVOICE_IS_SUCCESS("TXN2011", "Get returns by invoice is success"),
  TXN2012_GET_RETURN_BY_ID_IS_SUCCESS("TXN2012", "Get return by ID is success");

  private final String code;
  private final String message;
}
