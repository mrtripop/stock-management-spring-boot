package com.mrtripop.location.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.location.constant.ErrorCode;
import com.mrtripop.location.interfaces.AddressService;
import com.mrtripop.location.models.dtos.AddressDTO;
import com.mrtripop.location.models.entities.Address;
import com.mrtripop.location.repositories.AddressRepository;
import com.mrtripop.location.utils.AddressUtil;
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
public class AddressServiceImpl implements AddressService {

  private final AddressRepository addressRepository;

  public AddressServiceImpl(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  @Override
  public List<Address> getAllAddress(BaseQueryParams queryParams) throws ApplicationException {
    try {
      Pageable pageable = DatabaseHelper.initPageableWithSort(queryParams);
      Page<Address> pagesAddress = addressRepository.findAll(pageable);
      return pagesAddress.getContent();
    } catch (Exception e) {
      log.error("Cannot select addresses: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Address getAddressById(Long addressId) throws ApplicationException {
    try {
      return addressRepository
          .findById(addressId)
          .orElseThrow(
              () ->
                  new ApplicationException(
                      ErrorCode.UAD5003_NOT_FOUND_ADDRESSES_FOR_ADDRESS_ID, HttpStatus.NOT_FOUND));
    } catch (Exception e) {
      log.error("Cannot select addresses by address ID({}): {}", addressId, e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5002_CANNOT_RETRIEVE_ADDRESSES_BY_ADDRESS_ID,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Address addNewAddress(AddressDTO newAddress) throws ApplicationException {
    try {
      Address address = AddressUtil.dtoToAddress(newAddress);
      return addressRepository.save(address);
    } catch (Exception e) {
      log.error("Cannot insert new address: {}", e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5001_CANNOT_RETRIEVE_ADDRESSES, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Address updateAddress(Long addressId, AddressDTO newAddress) throws ApplicationException {
    try {
      Address existingAddress = getAddressById(addressId);
      AddressUtil.updateAddress(existingAddress, newAddress);
      return addressRepository.save(existingAddress);
    } catch (Exception e) {
      log.error("Cannot update for address ID '{}': {}", addressId, e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5005_CANNOT_UPDATE_THE_EXISTING_ADDRESS_ID,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void deleteAddressById(Long addressId) throws ApplicationException {
    try {
      Address existingAddress = getAddressById(addressId);
      addressRepository.delete(existingAddress);
    } catch (Exception e) {
      log.error("Cannot delete the address ID '{}': {}", addressId, e.getMessage());
      throw new ApplicationException(
          ErrorCode.UAD5007_CANNOT_DELETE_THE_ADDRESS_ID, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
