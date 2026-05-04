package com.mrtripop.clinical.services.impl;

import com.mrtripop.clinical.component.StoreMapper;
import com.mrtripop.clinical.constant.ErrorCode;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.dto.CreateStoreRequest;
import com.mrtripop.clinical.models.dto.StoreDto;
import com.mrtripop.clinical.models.dto.UpdateStoreRequest;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.clinical.services.StoreService;
import com.mrtripop.exception.ApplicationException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
  private final StoreRepository storeRepository;
  private final StoreMapper storeMapper;

  @Override
  @Transactional(readOnly = true)
  public Page<StoreDto> findAll(Pageable pageable) {
    return storeRepository.findByActiveTrue(pageable).map(storeMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public StoreDto findById(UUID id) throws ApplicationException {
    Store store = storeRepository.findByIdAndActiveTrue(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return storeMapper.toDto(store);
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public StoreDto create(CreateStoreRequest request) throws ApplicationException {
    if (storeRepository.existsByNameAndActiveTrue(request.getName())) {
      throw new ApplicationException(ErrorCode.DUPLICATE_STORE_NAME, HttpStatus.CONFLICT);
    }
    Store store = storeMapper.toEntity(request);
    Store saved = storeRepository.save(store);
    return storeMapper.toDto(saved);
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public StoreDto update(UUID id, UpdateStoreRequest request) throws ApplicationException {
    Store store = storeRepository.findByIdAndActiveTrue(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND));

    if (request.getName() != null && !request.getName().equals(store.getName())) {
      if (storeRepository.existsByNameAndActiveTrue(request.getName())) {
        throw new ApplicationException(ErrorCode.DUPLICATE_STORE_NAME, HttpStatus.CONFLICT);
      }
    }

    storeMapper.partialUpdate(request, store);
    Store saved = storeRepository.save(store);
    return storeMapper.toDto(saved);
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public void delete(UUID id) throws ApplicationException {
    Store store = storeRepository.findByIdAndActiveTrue(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND));
    store.setActive(false);
    storeRepository.save(store);
  }
}
