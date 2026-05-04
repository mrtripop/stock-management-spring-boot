package com.mrtripop.inventory.component;

import com.mrtripop.inventory.models.db.Task;
import com.mrtripop.inventory.models.dto.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "taskType", target = "taskType")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "batch.id", target = "batchId")
    @Mapping(source = "batch.batchNumber", target = "batchNumber")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.brandName", target = "brandName")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    TaskDto toDto(Task task);
}