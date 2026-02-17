package com.mrtripop.location.interfaces;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.location.models.dtos.AddressDTO;
import com.mrtripop.location.models.entities.Address;
import com.mrtripop.model.BaseQueryParams;
import java.util.List;

public interface AddressService {
  List<Address> getAllAddress(BaseQueryParams queryParams) throws ApplicationException;

  Address getAddressById(Long addressId) throws ApplicationException;

  Address addNewAddress(AddressDTO newAddress) throws ApplicationException;

  Address updateAddress(Long addressId, AddressDTO newAddress) throws ApplicationException;

  void deleteAddressById(Long addressId) throws ApplicationException;
}
