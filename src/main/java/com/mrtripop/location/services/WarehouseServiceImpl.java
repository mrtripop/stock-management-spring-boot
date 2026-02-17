package com.mrtripop.location.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.location.constant.ErrorCode;
import com.mrtripop.location.interfaces.AddressService;
import com.mrtripop.location.interfaces.WarehouseService;
import com.mrtripop.location.models.dtos.WarehouseDTO;
import com.mrtripop.location.models.entities.Address;
import com.mrtripop.location.models.entities.Warehouse;
import com.mrtripop.location.repositories.WarehouseRepository;
import com.mrtripop.location.utils.WarehouseUtil;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.util.DatabaseHelper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WarehouseServiceImpl implements WarehouseService {

  private final WarehouseRepository warehouseRepository;
  private final AddressService addressService;

  public WarehouseServiceImpl(
      WarehouseRepository warehouseRepository, AddressServiceImpl addressService) {
    this.warehouseRepository = warehouseRepository;
    this.addressService = addressService;
  }

  // Features
  // - getWarehouses -> admin
  // - getWarehouseByWarehouseID -> admin, manager, operator
  // - getWarehousesByAddressID -> admin, manager
  // - addWarehouse
  @Override
  public List<Warehouse> getAllWarehouse(BaseQueryParams queryParams) throws ApplicationException {
    try {
      Pageable pageable = DatabaseHelper.initPageableWithSort(queryParams);
      Page<Warehouse> warehousePages = warehouseRepository.findAll(pageable);
      return warehousePages.getContent();
    } catch (Exception e) {
      log.error("Cannot select all warehouse from database: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Warehouse getWarehouseById(Long warehouseId) throws ApplicationException {
    try {
      return warehouseRepository
          .findById(warehouseId)
          .orElseThrow(
              () ->
                  new ApplicationException(
                      ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.NOT_FOUND));
    } catch (Exception e) {
      log.error("Cannot select warehouse by warehouse Id({}): {}", warehouseId, e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Warehouse addNewWarehouse(WarehouseDTO newWarehouse) throws ApplicationException {
    try {
      Address address = addressService.getAddressById(newWarehouse.getAddressId());
      Warehouse warehouse = WarehouseUtil.dtoToWarehouse(newWarehouse);
      warehouse.setAddress(address);
      return warehouseRepository.save(warehouse);
    } catch (Exception e) {
      log.error("Cannot add a new warehouse: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Warehouse updateWarehouse(Long warehouseId, WarehouseDTO newWarehouseDTO)
      throws ApplicationException {
    try {
      Address address = addressService.getAddressById(newWarehouseDTO.getAddressId());
      Warehouse existingWarehouse = getWarehouseById(warehouseId);
      Warehouse newWarehouse = WarehouseUtil.dtoToWarehouse(newWarehouseDTO);
      newWarehouse.setAddress(address);
      WarehouseUtil.updateWarehouse(existingWarehouse, newWarehouse);
      return warehouseRepository.save(existingWarehouse);
    } catch (Exception e) {
      log.error("Cannot update the warehouse ID '{}': {}", warehouseId, e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void deleteWarehouse(Long warehouseId) throws ApplicationException {
    try {
      Warehouse existingWarehouse = getWarehouseById(warehouseId);
      warehouseRepository.delete(existingWarehouse);
    } catch (Exception e) {
      log.error("Cannot delete the warehouse ID '{}': {}", warehouseId, e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
