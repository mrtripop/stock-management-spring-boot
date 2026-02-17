package com.mrtripop.location.interfaces;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.location.models.dtos.WarehouseDTO;
import com.mrtripop.location.models.entities.Warehouse;
import com.mrtripop.model.BaseQueryParams;
import java.util.List;

public interface WarehouseService {

  List<Warehouse> getAllWarehouse(BaseQueryParams queryParams) throws ApplicationException;

  Warehouse getWarehouseById(Long warehouseId) throws ApplicationException;

  Warehouse addNewWarehouse(WarehouseDTO newWarehouse) throws ApplicationException;

  Warehouse updateWarehouse(Long warehouseId, WarehouseDTO newWarehouse) throws ApplicationException;

  void deleteWarehouse(Long warehouseId) throws ApplicationException;
}
