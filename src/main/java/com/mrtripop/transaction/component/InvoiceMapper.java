package com.mrtripop.transaction.component;

import com.mrtripop.transaction.models.db.Invoice;
import com.mrtripop.transaction.models.db.InvoiceItem;
import com.mrtripop.transaction.models.dto.InvoiceDto;
import com.mrtripop.transaction.models.dto.InvoiceItemDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

  @Mapping(target = "storeId", source = "store.id")
  @Mapping(target = "storeName", source = "store.name")
  @Mapping(target = "items", ignore = true)
  InvoiceDto toDto(Invoice invoice);

  @Mapping(target = "brandName", source = "brand.brandName")
  @Mapping(target = "batchNumber", source = "batch.batchNumber")
  InvoiceItemDto toItemDto(InvoiceItem item);

  List<InvoiceItemDto> toItemDtoList(List<InvoiceItem> items);
}
