package com.mrtripop.location.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.location.constant.ErrorCode;
import com.mrtripop.location.constant.SuccessCode;
import com.mrtripop.location.interfaces.AddressService;
import com.mrtripop.location.models.dtos.AddressDTO;
import com.mrtripop.location.models.entities.Address;
import com.mrtripop.location.services.AddressServiceImpl;
import com.mrtripop.location.utils.AddressUtil;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/inventory/addresses")
public class AddressController {

  private final AddressService addressService;

  public AddressController(AddressServiceImpl addressService) {
    this.addressService = addressService;
  }

  @GetMapping
  public ResponseEntity<Object> getAddresses(@Valid BaseQueryParams queryParams)
      throws ApplicationException {
    try {
      List<Address> addresses = addressService.getAllAddress(queryParams);
      List<AddressDTO> response = addresses.stream().map(AddressUtil::addressToDTO).toList();
      BaseStatusCode code = SuccessCode.SUCCESS;
      return ResponseBody.builder()
          .code(code.getCode())
          .message(code.getMessage())
          .data(response)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot get addresses: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{addressId}")
  public ResponseEntity<Object> getAddressesById(
      @PathVariable @Min(value = 1, message = "Address ID is invalid") Long addressId)
      throws ApplicationException {
    try {
      Address address = addressService.getAddressById(addressId);
      AddressDTO response = AddressUtil.addressToDTO(address);
      BaseStatusCode code = SuccessCode.SUCCESS;
      return ResponseBody.builder()
          .code(code.getCode())
          .message(code.getMessage())
          .data(response)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot get an addresses: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5003_NOT_FOUND_ADDRESSES_FOR_ADDRESS_ID, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping
  public ResponseEntity<Object> addNewAddress(@RequestBody @Valid AddressDTO addressDTO)
      throws ApplicationException {
    try {
      Address createdAddress = addressService.addNewAddress(addressDTO);
      AddressDTO response = AddressUtil.addressToDTO(createdAddress);
      BaseStatusCode code = SuccessCode.SUCCESS;
      return ResponseBody.builder()
          .code(code.getCode())
          .message(code.getMessage())
          .data(response)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot create a new address: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5003_NOT_FOUND_ADDRESSES_FOR_ADDRESS_ID, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/{addressId}")
  public ResponseEntity<Object> updateAddress(
      @PathVariable(name = "addressId") Long addressId,
      @RequestBody @Valid AddressDTO addressDetails)
      throws ApplicationException {
    try {
      Address updatedAddress = addressService.updateAddress(addressId, addressDetails);
      AddressDTO response = AddressUtil.addressToDTO(updatedAddress);
      BaseStatusCode code = SuccessCode.SUCCESS;
      return ResponseBody.builder()
          .code(code.getCode())
          .message(code.getMessage())
          .data(response)
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot update the existing address: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5004_CANNOT_UPDATE_THE_ADDRESS, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{addressId}")
  public ResponseEntity<Object> deleteAddress(@PathVariable(name = "addressId") Long addressId)
      throws ApplicationException {
    try {
      addressService.deleteAddressById(addressId);
      BaseStatusCode code = SuccessCode.SUCCESS;
      return ResponseBody.builder()
          .code(code.getCode())
          .message(code.getMessage())
          .build()
          .toResponseEntity(HttpStatus.OK);
    } catch (Exception e) {
      log.error("Cannot delete the address: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5006_CANNOT_DELETE_THE_ADDRESS, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
