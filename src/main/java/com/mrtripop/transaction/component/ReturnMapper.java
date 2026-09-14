package com.mrtripop.transaction.component;

import com.mrtripop.transaction.models.db.Return;
import com.mrtripop.transaction.models.db.ReturnItem;
import com.mrtripop.transaction.models.dto.ReturnDto;
import com.mrtripop.transaction.models.dto.ReturnItemDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReturnMapper {

  @Mapping(target = "invoiceId", source = "invoice.id")
  @Mapping(target = "items", ignore = true)
  ReturnDto toDto(Return returnEntity);

  @Mapping(target = "invoiceItemId", source = "invoiceItem.id")
  @Mapping(target = "brandName", source = "invoiceItem.brand.brandName")
  @Mapping(target = "batchNumber", source = "invoiceItem.batch.batchNumber")
  ReturnItemDto toItemDto(ReturnItem item);

  List<ReturnItemDto> toItemDtoList(List<ReturnItem> items);
}
