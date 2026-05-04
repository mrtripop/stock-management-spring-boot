package com.mrtripop.clinical.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.component.StoreMapper;
import com.mrtripop.clinical.constant.ErrorCode;
import com.mrtripop.clinical.fixture.StoreFixture;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.clinical.models.dto.CreateStoreRequest;
import com.mrtripop.clinical.models.dto.StoreDto;
import com.mrtripop.clinical.models.dto.UpdateStoreRequest;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.exception.ApplicationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StoreServiceImpl")
class StoreServiceImplTest {

  @Mock private StoreRepository storeRepository;
  @Mock private StoreMapper storeMapper;
  @InjectMocks private StoreServiceImpl storeService;

  @Nested
  @DisplayName("CreateStore")
  class CreateStore {

    @Test
    @DisplayName("should create store and return DTO")
    void shouldCreateStore() throws ApplicationException {
      CreateStoreRequest request = StoreFixture.validCreateRequest();
      Store saved = StoreFixture.defaultEntity();
      StoreDto dto = StoreDto.builder().id(StoreFixture.STORE_ID).name(StoreFixture.STORE_NAME).type(StoreType.PHYSICAL).build();

      when(storeRepository.existsByNameAndActiveTrue(StoreFixture.STORE_NAME)).thenReturn(false);
      when(storeMapper.toEntity(request)).thenReturn(saved);
      when(storeRepository.save(any(Store.class))).thenReturn(saved);
      when(storeMapper.toDto(saved)).thenReturn(dto);

      StoreDto result = storeService.create(request);

      assertNotNull(result.getId());
      assertEquals(StoreFixture.STORE_NAME, result.getName());
      verify(storeRepository).save(any(Store.class));
    }

    @Test
    @DisplayName("should throw CL4002 when store name already exists")
    void shouldThrowDuplicateName() {
      CreateStoreRequest request = StoreFixture.validCreateRequest();
      when(storeRepository.existsByNameAndActiveTrue(StoreFixture.STORE_NAME)).thenReturn(true);

      ApplicationException ex = assertThrows(ApplicationException.class, () -> storeService.create(request));
      assertEquals(ErrorCode.DUPLICATE_STORE_NAME, ex.getErrorCode());
      verify(storeRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("FindAllStores")
  class FindAllStores {

    @Test
    @DisplayName("should return paginated list of stores")
    void shouldReturnPaginatedList() {
      Store store = StoreFixture.defaultEntity();
      StoreDto dto = StoreDto.builder().id(StoreFixture.STORE_ID).name(StoreFixture.STORE_NAME).type(StoreType.PHYSICAL).build();
      Page<Store> page = new PageImpl<>(List.of(store));
      when(storeRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);
      when(storeMapper.toDto(store)).thenReturn(dto);

      Page<StoreDto> result = storeService.findAll(Pageable.unpaged());

      assertEquals(1, result.getTotalElements());
      assertEquals(StoreFixture.STORE_NAME, result.getContent().get(0).getName());
    }
  }

  @Nested
  @DisplayName("FindStoreById")
  class FindStoreById {

    @Test
    @DisplayName("should return store DTO when found")
    void shouldReturnStoreDto() throws ApplicationException {
      Store store = StoreFixture.defaultEntity();
      StoreDto dto = StoreDto.builder().id(StoreFixture.STORE_ID).name(StoreFixture.STORE_NAME).type(StoreType.PHYSICAL).build();
      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(storeMapper.toDto(store)).thenReturn(dto);

      StoreDto result = storeService.findById(StoreFixture.STORE_ID);

      assertEquals(StoreFixture.STORE_ID, result.getId());
    }

    @Test
    @DisplayName("should throw CL4001 when store not found")
    void shouldThrowNotFound() {
      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.empty());

      ApplicationException ex = assertThrows(ApplicationException.class, () -> storeService.findById(StoreFixture.STORE_ID));
      assertEquals(ErrorCode.STORE_NOT_FOUND, ex.getErrorCode());
    }
  }

  @Nested
  @DisplayName("UpdateStore")
  class UpdateStore {

    @Test
    @DisplayName("should update store name only")
    void shouldUpdateNameOnly() throws ApplicationException {
      Store existing = StoreFixture.defaultEntity();
      UpdateStoreRequest request = StoreFixture.updateNameRequest();
      StoreDto dto = StoreDto.builder().id(StoreFixture.STORE_ID).name(StoreFixture.STORE_NAME_UPDATED).type(StoreType.PHYSICAL).build();

      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.of(existing));
      when(storeRepository.existsByNameAndActiveTrue(StoreFixture.STORE_NAME_UPDATED)).thenReturn(false);
      when(storeRepository.save(any(Store.class))).thenReturn(existing);
      when(storeMapper.toDto(any(Store.class))).thenReturn(dto);

      StoreDto result = storeService.update(StoreFixture.STORE_ID, request);
      assertEquals(StoreFixture.STORE_NAME_UPDATED, result.getName());
      verify(storeMapper).partialUpdate(request, existing);
    }

    @Test
    @DisplayName("should update store type only")
    void shouldUpdateTypeOnly() throws ApplicationException {
      Store existing = StoreFixture.defaultEntity();
      UpdateStoreRequest request = StoreFixture.updateTypeRequest();
      StoreDto dto = StoreDto.builder().id(StoreFixture.STORE_ID).name(StoreFixture.STORE_NAME).type(StoreType.LOGICAL).build();

      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.of(existing));
      when(storeRepository.save(any(Store.class))).thenReturn(existing);
      when(storeMapper.toDto(any(Store.class))).thenReturn(dto);

      StoreDto result = storeService.update(StoreFixture.STORE_ID, request);
      assertEquals(StoreType.LOGICAL, result.getType());
      verify(storeMapper).partialUpdate(request, existing);
    }

    @Test
    @DisplayName("should update both name and type")
    void shouldUpdateBothNameAndType() throws ApplicationException {
      Store existing = StoreFixture.defaultEntity();
      UpdateStoreRequest request = StoreFixture.updateBothRequest();
      StoreDto dto = StoreDto.builder().id(StoreFixture.STORE_ID).name(StoreFixture.STORE_NAME_UPDATED).type(StoreType.LOGICAL).build();

      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.of(existing));
      when(storeRepository.existsByNameAndActiveTrue(StoreFixture.STORE_NAME_UPDATED)).thenReturn(false);
      when(storeRepository.save(any(Store.class))).thenReturn(existing);
      when(storeMapper.toDto(any(Store.class))).thenReturn(dto);

      StoreDto result = storeService.update(StoreFixture.STORE_ID, request);
      assertEquals(StoreFixture.STORE_NAME_UPDATED, result.getName());
      assertEquals(StoreType.LOGICAL, result.getType());
      verify(storeMapper).partialUpdate(request, existing);
    }

    @Test
    @DisplayName("should throw CL4002 when updating to duplicate name")
    void shouldThrowDuplicateNameOnUpdate() throws ApplicationException {
      Store existing = StoreFixture.defaultEntity();
      UpdateStoreRequest request = StoreFixture.updateNameRequest();

      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.of(existing));
      when(storeRepository.existsByNameAndActiveTrue(StoreFixture.STORE_NAME_UPDATED)).thenReturn(true);

      ApplicationException ex = assertThrows(ApplicationException.class, () -> storeService.update(StoreFixture.STORE_ID, request));
      assertEquals(ErrorCode.DUPLICATE_STORE_NAME, ex.getErrorCode());
      verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw CL4001 when store not found for update")
    void shouldThrowNotFoundForUpdate() {
      UpdateStoreRequest request = StoreFixture.updateNameRequest();
      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.empty());

      ApplicationException ex = assertThrows(ApplicationException.class, () -> storeService.update(StoreFixture.STORE_ID, request));
      assertEquals(ErrorCode.STORE_NOT_FOUND, ex.getErrorCode());
    }
  }

  @Nested
  @DisplayName("DeleteStore")
  class DeleteStore {

    @Test
    @DisplayName("should soft-delete store by setting active to false")
    void shouldSoftDeleteStore() throws ApplicationException {
      Store store = StoreFixture.defaultEntity();
      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.of(store));
      when(storeRepository.save(any(Store.class))).thenReturn(store);

      assertDoesNotThrow(() -> storeService.delete(StoreFixture.STORE_ID));
      verify(storeRepository).save(store);
      assertFalse(store.isActive());
    }

    @Test
    @DisplayName("should throw CL4001 when store not found for delete")
    void shouldThrowNotFoundForDelete() {
      when(storeRepository.findByIdAndActiveTrue(StoreFixture.STORE_ID)).thenReturn(Optional.empty());

      ApplicationException ex = assertThrows(ApplicationException.class, () -> storeService.delete(StoreFixture.STORE_ID));
      assertEquals(ErrorCode.STORE_NOT_FOUND, ex.getErrorCode());
    }
  }
}
