package com.mrtripop.clinical.services;

import com.mrtripop.clinical.models.dto.CreateStoreRequest;
import com.mrtripop.clinical.models.dto.StoreDto;
import com.mrtripop.clinical.models.dto.UpdateStoreRequest;
import com.mrtripop.exception.ApplicationException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreService {
  Page<StoreDto> findAll(Pageable pageable);
  StoreDto findById(UUID id) throws ApplicationException;
  StoreDto create(CreateStoreRequest request) throws ApplicationException;
  StoreDto update(UUID id, UpdateStoreRequest request) throws ApplicationException;
  void delete(UUID id) throws ApplicationException;
}