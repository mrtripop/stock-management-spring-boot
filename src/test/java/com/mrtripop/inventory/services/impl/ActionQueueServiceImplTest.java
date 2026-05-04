package com.mrtripop.inventory.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.TaskMapper;
import com.mrtripop.inventory.config.ActionQueueProperties;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.fixture.TaskFixture;
import com.mrtripop.inventory.models.db.*;
import com.mrtripop.inventory.models.dto.ActionQueueScanResult;
import com.mrtripop.inventory.models.dto.TaskDto;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.repository.TaskRepository;
import java.time.LocalDate;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActionQueueServiceImpl")
class ActionQueueServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private StoreStockRepository storeStockRepository;
    @Mock private StoreProductRepository storeProductRepository;
    @Mock private AuditService auditService;
    @Mock private TaskMapper taskMapper;
    @Mock private ActionQueueProperties properties;

    @InjectMocks private ActionQueueServiceImpl actionQueueService;

    private final UUID storeId = UUID.randomUUID();
    private final Pageable pageable = PageRequest.of(0, 20);

    private Store store;
    private Brand brand;
    private Task pendingTask;

    private void setupCommonFixtures() {
        store = Store.builder().id(storeId).name("Test Store").build();
        brand = Brand.builder().id(UUID.randomUUID()).brandName("Test Brand").build();
        pendingTask = TaskFixture.pendingExpiryWarningTask(store, null, brand).build();
        pendingTask.setId(1L);
    }

    @Nested
    @DisplayName("getTasks")
    class GetTasks {

        @Test
        @DisplayName("should return filtered page when status filter is provided")
        void getTasks_withStatusFilter_returnsFilteredPage() {
            setupCommonFixtures();
            Page<Task> taskPage = new PageImpl<>(List.of(pendingTask));
            TaskDto dto = TaskDto.builder().id(1L).build();
            when(taskRepository.findByStore_IdAndStatus(eq(storeId), eq(TaskStatus.PENDING), eq(pageable)))
                    .thenReturn(taskPage);
            when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

            Page<TaskDto> result = actionQueueService.getTasks(storeId, TaskStatus.PENDING, pageable);

            assertEquals(1, result.getTotalElements());
            verify(taskRepository).findByStore_IdAndStatus(storeId, TaskStatus.PENDING, pageable);
        }

        @Test
        @DisplayName("should return all tasks when no status filter is provided")
        void getTasks_withoutStatusFilter_returnsAllTasks() {
            setupCommonFixtures();
            Page<Task> taskPage = new PageImpl<>(List.of(pendingTask));
            TaskDto dto = TaskDto.builder().id(1L).build();
            when(taskRepository.findByStore_Id(eq(storeId), eq(pageable))).thenReturn(taskPage);
            when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

            Page<TaskDto> result = actionQueueService.getTasks(storeId, null, pageable);

            assertEquals(1, result.getTotalElements());
            verify(taskRepository).findByStore_Id(storeId, pageable);
        }
    }

    @Nested
    @DisplayName("getTaskById")
    class GetTaskById {

        @Test
        @DisplayName("should return DTO when task exists")
        void getTaskById_found_returnsDto() throws ApplicationException {
            setupCommonFixtures();
            TaskDto dto = TaskDto.builder().id(1L).build();
            when(taskRepository.findWithDetailsById(1L)).thenReturn(Optional.of(pendingTask));
            when(taskMapper.toDto(pendingTask)).thenReturn(dto);

            TaskDto result = actionQueueService.getTaskById(1L);

            assertNotNull(result);
            verify(taskRepository).findWithDetailsById(1L);
        }

        @Test
        @DisplayName("should throw TASK_NOT_FOUND when task does not exist")
        void getTaskById_notFound_throwsException() {
            when(taskRepository.findWithDetailsById(999L)).thenReturn(Optional.empty());

            ApplicationException ex = assertThrows(ApplicationException.class,
                    () -> actionQueueService.getTaskById(999L));
            assertEquals(ErrorCode.TASK_NOT_FOUND, ex.getErrorCode());
            assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
        }
    }

    @Nested
    @DisplayName("acknowledgeTask")
    class AcknowledgeTask {

        @Test
        @DisplayName("should transition PENDING task to ACKNOWLEDGED")
        void acknowledgeTask_pendingTask_transitionsToAcknowledged() throws ApplicationException {
            setupCommonFixtures();
            TaskDto dto = TaskDto.builder().id(1L).status("ACKNOWLEDGED").build();
            when(taskRepository.findWithDetailsById(1L)).thenReturn(Optional.of(pendingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(pendingTask);
            when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

            TaskDto result = actionQueueService.acknowledgeTask(1L);

            assertEquals("ACKNOWLEDGED", result.getStatus());
            verify(auditService).recordAudit(eq("ACTION_QUEUE_ACKNOWLEDGED"), anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw TASK_ALREADY_RESOLVED when task is RESOLVED")
        void acknowledgeTask_resolvedTask_throwsException() {
            setupCommonFixtures();
            Task resolvedTask = TaskFixture.resolvedTask(store, brand).build();
            resolvedTask.setId(2L);
            when(taskRepository.findWithDetailsById(2L)).thenReturn(Optional.of(resolvedTask));

            ApplicationException ex = assertThrows(ApplicationException.class,
                    () -> actionQueueService.acknowledgeTask(2L));
            assertEquals(ErrorCode.TASK_ALREADY_RESOLVED, ex.getErrorCode());
        }

        @Test
        @DisplayName("should throw INVALID_TASK_STATUS when task is ACKNOWLEDGED")
        void acknowledgeTask_acknowledgedTask_throwsInvalidStatus() {
            setupCommonFixtures();
            Task acknowledgedTask = TaskFixture.acknowledgedTask(store, brand).build();
            acknowledgedTask.setId(3L);
            when(taskRepository.findWithDetailsById(3L)).thenReturn(Optional.of(acknowledgedTask));

            ApplicationException ex = assertThrows(ApplicationException.class,
                    () -> actionQueueService.acknowledgeTask(3L));
            assertEquals(ErrorCode.INVALID_TASK_STATUS, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("resolveTask")
    class ResolveTask {

        @Test
        @DisplayName("should transition PENDING task to RESOLVED")
        void resolveTask_pendingTask_transitionsToResolved() throws ApplicationException {
            setupCommonFixtures();
            TaskDto dto = TaskDto.builder().id(1L).status("RESOLVED").build();
            when(taskRepository.findWithDetailsById(1L)).thenReturn(Optional.of(pendingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(pendingTask);
            when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

            TaskDto result = actionQueueService.resolveTask(1L);

            assertEquals("RESOLVED", result.getStatus());
        }

        @Test
        @DisplayName("should transition ACKNOWLEDGED task to RESOLVED")
        void resolveTask_acknowledgedTask_transitionsToResolved() throws ApplicationException {
            setupCommonFixtures();
            Task acknowledgedTask = TaskFixture.acknowledgedTask(store, brand).build();
            acknowledgedTask.setId(3L);
            TaskDto dto = TaskDto.builder().id(3L).status("RESOLVED").build();
            when(taskRepository.findWithDetailsById(3L)).thenReturn(Optional.of(acknowledgedTask));
            when(taskRepository.save(any(Task.class))).thenReturn(acknowledgedTask);
            when(taskMapper.toDto(any(Task.class))).thenReturn(dto);

            TaskDto result = actionQueueService.resolveTask(3L);

            assertEquals("RESOLVED", result.getStatus());
        }

        @Test
        @DisplayName("should throw TASK_ALREADY_RESOLVED when task is already RESOLVED")
        void resolveTask_alreadyResolved_throwsException() {
            setupCommonFixtures();
            Task resolvedTask = TaskFixture.resolvedTask(store, brand).build();
            resolvedTask.setId(2L);
            when(taskRepository.findWithDetailsById(2L)).thenReturn(Optional.of(resolvedTask));

            ApplicationException ex = assertThrows(ApplicationException.class,
                    () -> actionQueueService.resolveTask(2L));
            assertEquals(ErrorCode.TASK_ALREADY_RESOLVED, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("runExpiryScan")
    class ExpiryScan {

        @Test
        @DisplayName("should create tasks for expiring stock")
        void runExpiryScan_createsTasksForExpiringStock() {
            setupCommonFixtures();
            Batch batch = Batch.builder().id(1L).batchNumber("BATCH-001")
                    .brand(brand).expiryDate(LocalDate.now().plusDays(15)).build();
            StoreStock stock = StoreStock.builder().id(1L).store(store).batch(batch).quantity(100L).build();

            when(properties.getExpiryWarningDays()).thenReturn(30);
            when(storeStockRepository.findDistinctStoreIds()).thenReturn(List.of(storeId));
            when(storeStockRepository.findExpiringSoonByStore(eq(storeId), any(LocalDate.class)))
                    .thenReturn(List.of(stock));
            when(taskRepository.findActiveExpiryTask(eq(storeId), eq(1L), eq(TaskType.EXPIRY_WARNING), anyList()))
                    .thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task t = inv.getArgument(0);
                if (t.getId() == null) t.setId(1L);
                return t;
            });

            ActionQueueScanResult result = actionQueueService.runExpiryScan();

            assertEquals(1, result.getExpiryWarningsCreated());
            assertEquals(0, result.getExpiryWarningsUpdated());
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("should update existing tasks instead of creating duplicates")
        void runExpiryScan_updatesExistingTasks() {
            setupCommonFixtures();
            Batch batch = Batch.builder().id(1L).batchNumber("BATCH-001")
                    .brand(brand).expiryDate(LocalDate.now().plusDays(15)).build();
            StoreStock stock = StoreStock.builder().id(1L).store(store).batch(batch).quantity(100L).build();
            Task existingTask = TaskFixture.pendingExpiryWarningTask(store, batch, brand).build();
            existingTask.setId(1L);

            when(properties.getExpiryWarningDays()).thenReturn(30);
            when(storeStockRepository.findDistinctStoreIds()).thenReturn(List.of(storeId));
            when(storeStockRepository.findExpiringSoonByStore(eq(storeId), any(LocalDate.class)))
                    .thenReturn(List.of(stock));
            when(taskRepository.findActiveExpiryTask(eq(storeId), eq(1L), eq(TaskType.EXPIRY_WARNING), anyList()))
                    .thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            ActionQueueScanResult result = actionQueueService.runExpiryScan();

            assertEquals(0, result.getExpiryWarningsCreated());
            assertEquals(1, result.getExpiryWarningsUpdated());
        }
    }

    @Nested
    @DisplayName("runReorderScan")
    class ReorderScan {

        @Test
        @DisplayName("should create tasks when stock is below threshold")
        void runReorderScan_createsTasksForLowStock() {
            setupCommonFixtures();
            StoreProduct sp = StoreProduct.builder()
                    .store(store).brand(brand).reorderThreshold(50L).build();
            when(storeProductRepository.findWithReorderThreshold()).thenReturn(List.of(sp));
            when(storeStockRepository.sumAvailableQuantityByStoreAndBrand(storeId, brand.getId())).thenReturn(10L);
            when(taskRepository.findActiveReorderTask(eq(storeId), eq(brand.getId()), eq(TaskType.REORDER_NEEDED), anyList()))
                    .thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task t = inv.getArgument(0);
                if (t.getId() == null) t.setId(1L);
                return t;
            });

            ActionQueueScanResult result = actionQueueService.runReorderScan();

            assertEquals(1, result.getReorderAlertsCreated());
            assertEquals(0, result.getReorderAlertsUpdated());
        }

        @Test
        @DisplayName("should skip when stock is at or above threshold")
        void runReorderScan_skipsSufficientStock() {
            setupCommonFixtures();
            StoreProduct sp = StoreProduct.builder()
                    .store(store).brand(brand).reorderThreshold(50L).build();
            when(storeProductRepository.findWithReorderThreshold()).thenReturn(List.of(sp));
            when(storeStockRepository.sumAvailableQuantityByStoreAndBrand(storeId, brand.getId())).thenReturn(100L);

            ActionQueueScanResult result = actionQueueService.runReorderScan();

            assertEquals(0, result.getReorderAlertsCreated());
        }

        @Test
        @DisplayName("should update existing reorder task instead of creating duplicate")
        void runReorderScan_updatesExistingTask() {
            setupCommonFixtures();
            StoreProduct sp = StoreProduct.builder()
                    .store(store).brand(brand).reorderThreshold(50L).build();
            Task existingTask = TaskFixture.pendingReorderNeededTask(store, brand, 5L, 50L).build();
            existingTask.setId(1L);

            when(storeProductRepository.findWithReorderThreshold()).thenReturn(List.of(sp));
            when(storeStockRepository.sumAvailableQuantityByStoreAndBrand(storeId, brand.getId())).thenReturn(3L);
            when(taskRepository.findActiveReorderTask(eq(storeId), eq(brand.getId()), eq(TaskType.REORDER_NEEDED), anyList()))
                    .thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            ActionQueueScanResult result = actionQueueService.runReorderScan();

            assertEquals(0, result.getReorderAlertsCreated());
            assertEquals(1, result.getReorderAlertsUpdated());
        }
    }

    @Nested
    @DisplayName("runFullScan")
    class FullScan {

        @Test
        @DisplayName("should combine results from both scans")
        void runFullScan_combinesBothScans() {
            setupCommonFixtures();
            Batch batch = Batch.builder().id(1L).batchNumber("BATCH-001")
                    .brand(brand).expiryDate(LocalDate.now().plusDays(15)).build();
            StoreStock stock = StoreStock.builder().id(1L).store(store).batch(batch).quantity(100L).build();
            StoreProduct sp = StoreProduct.builder()
                    .store(store).brand(brand).reorderThreshold(50L).build();

            when(properties.getExpiryWarningDays()).thenReturn(30);
            when(storeStockRepository.findDistinctStoreIds()).thenReturn(List.of(storeId));
            when(storeStockRepository.findExpiringSoonByStore(eq(storeId), any(LocalDate.class)))
                    .thenReturn(List.of(stock));
            when(taskRepository.findActiveExpiryTask(eq(storeId), eq(1L), eq(TaskType.EXPIRY_WARNING), anyList()))
                    .thenReturn(Optional.empty());
            when(storeProductRepository.findWithReorderThreshold()).thenReturn(List.of(sp));
            when(storeStockRepository.sumAvailableQuantityByStoreAndBrand(storeId, brand.getId())).thenReturn(10L);
            when(taskRepository.findActiveReorderTask(eq(storeId), eq(brand.getId()), eq(TaskType.REORDER_NEEDED), anyList()))
                    .thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task t = inv.getArgument(0);
                if (t.getId() == null) t.setId(1L);
                return t;
            });

            ActionQueueScanResult result = actionQueueService.runFullScan();

            assertNotNull(result);
            assertEquals(1, result.getExpiryWarningsCreated());
            assertEquals(1, result.getReorderAlertsCreated());
        }

        @Test
        @DisplayName("should return empty result when no alerts needed")
        void runFullScan_returnsEmptyWhenNoAlerts() {
            setupCommonFixtures();
            when(storeStockRepository.findDistinctStoreIds()).thenReturn(List.of());
            when(storeProductRepository.findWithReorderThreshold()).thenReturn(List.of());
            when(properties.getExpiryWarningDays()).thenReturn(30);

            ActionQueueScanResult result = actionQueueService.runFullScan();

            assertNotNull(result);
            assertEquals(0, result.getExpiryWarningsCreated());
            assertEquals(0, result.getReorderAlertsCreated());
        }
    }
}
