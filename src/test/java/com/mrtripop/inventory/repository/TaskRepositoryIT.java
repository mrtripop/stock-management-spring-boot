package com.mrtripop.inventory.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Molecule;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.clinical.models.db.StoreType;
import com.mrtripop.clinical.repository.BrandRepository;
import com.mrtripop.clinical.repository.MoleculeRepository;
import com.mrtripop.clinical.repository.StoreRepository;
import com.mrtripop.inventory.models.db.Batch;
import com.mrtripop.inventory.models.db.BatchStatus;
import com.mrtripop.inventory.models.db.Task;
import com.mrtripop.inventory.models.db.TaskStatus;
import com.mrtripop.inventory.models.db.TaskType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TaskRepository")
class TaskRepositoryIT {

    @Autowired private TaskRepository taskRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private MoleculeRepository moleculeRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private com.mrtripop.inventory.repository.BatchRepository batchRepository;

    private Store store;
    private Molecule molecule;
    private Brand brand;
    private Batch batch;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(Store.builder()
                .name("Test Store")
                .type(StoreType.PHYSICAL)
                .build());

        molecule = moleculeRepository.save(Molecule.builder()
                .genericName("Test Molecule")
                .build());

        brand = brandRepository.save(Brand.builder()
                .molecule(molecule)
                .brandName("Test Brand")
                .baseUnit("TABLET")
                .build());

        batch = batchRepository.save(Batch.builder()
                .brand(brand)
                .batchNumber("BATCH-001")
                .expiryDate(LocalDate.now().plusDays(60))
                .quantity(100L)
                .status(BatchStatus.AVAILABLE)
                .build());
    }

    @Nested
    @DisplayName("save and retrieve")
    class SaveAndRetrieve {

        @Test
        @DisplayName("should persist and retrieve task with relationships")
        void saveAndRetrieveTask() {
            Task task = Task.builder()
                    .store(store)
                    .taskType(TaskType.EXPIRY_WARNING)
                    .status(TaskStatus.PENDING)
                    .batch(batch)
                    .brand(brand)
                    .message("Batch BATCH-001 expires in 60 days")
                    .daysUntilExpiry(60)
                    .build();
            task = taskRepository.save(task);

            Optional<Task> found = taskRepository.findById(task.getId());
            assertTrue(found.isPresent());
            assertEquals("EXPIRY_WARNING", found.get().getTaskType().name());
            assertEquals("PENDING", found.get().getStatus().name());
            assertEquals(60, found.get().getDaysUntilExpiry());
            assertNotNull(found.get().getCreatedAt());
        }
    }

    @Nested
    @DisplayName("findActiveTask")
    class FindActiveTask {

        @Test
        @DisplayName("should return PENDING task for matching store and batch")
        void findActiveExpiryTask_returnsPendingTask() {
            Task task = Task.builder()
                    .store(store)
                    .taskType(TaskType.EXPIRY_WARNING)
                    .status(TaskStatus.PENDING)
                    .batch(batch)
                    .brand(brand)
                    .message("Test expiry")
                    .daysUntilExpiry(15)
                    .build();
            task = taskRepository.save(task);

            Optional<Task> found = taskRepository.findActiveExpiryTask(
                    store.getId(), batch.getId(), TaskType.EXPIRY_WARNING,
                    List.of(TaskStatus.PENDING, TaskStatus.ACKNOWLEDGED));

            assertTrue(found.isPresent());
            assertEquals(task.getId(), found.get().getId());
        }

        @Test
        @DisplayName("should return PENDING reorder task for matching store and brand")
        void findActiveReorderTask_returnsPendingTask() {
            Task task = Task.builder()
                    .store(store)
                    .taskType(TaskType.REORDER_NEEDED)
                    .status(TaskStatus.PENDING)
                    .brand(brand)
                    .message("Low stock")
                    .currentQuantity(5L)
                    .thresholdQuantity(50L)
                    .build();
            task = taskRepository.save(task);

            Optional<Task> found = taskRepository.findActiveReorderTask(
                    store.getId(), brand.getId(), TaskType.REORDER_NEEDED,
                    List.of(TaskStatus.PENDING, TaskStatus.ACKNOWLEDGED));

            assertTrue(found.isPresent());
            assertEquals(task.getId(), found.get().getId());
        }

        @Test
        @DisplayName("should not return RESOLVED task")
        void findActiveExpiryTask_doesNotReturnResolvedTask() {
            Task task = Task.builder()
                    .store(store)
                    .taskType(TaskType.EXPIRY_WARNING)
                    .status(TaskStatus.RESOLVED)
                    .batch(batch)
                    .brand(brand)
                    .message("Resolved")
                    .daysUntilExpiry(5)
                    .build();
            taskRepository.save(task);

            Optional<Task> found = taskRepository.findActiveExpiryTask(
                    store.getId(), batch.getId(), TaskType.EXPIRY_WARNING,
                    List.of(TaskStatus.PENDING, TaskStatus.ACKNOWLEDGED));

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("paginated queries")
    class PaginatedQueries {

        @Test
        @DisplayName("should return paged results filtered by store and status")
        void findByStore_IdAndStatus_returnsPagedResults() {
            for (int i = 0; i < 3; i++) {
                Task task = Task.builder()
                        .store(store)
                        .taskType(TaskType.EXPIRY_WARNING)
                        .status(TaskStatus.PENDING)
                        .brand(brand)
                        .message("Task " + i)
                        .build();
                taskRepository.save(task);
            }

            Page<Task> page = taskRepository.findByStore_IdAndStatus(
                    store.getId(), TaskStatus.PENDING, PageRequest.of(0, 10));

            assertTrue(page.getTotalElements() >= 3);
            page.getContent().forEach(t -> assertEquals(TaskStatus.PENDING, t.getStatus()));
        }
    }

    @Nested
    @DisplayName("enum persistence")
    class EnumPersistence {

        @Test
        @DisplayName("should persist TaskType and TaskStatus as strings")
        void taskTypePersistsAsString() {
            Task task = Task.builder()
                    .store(store)
                    .taskType(TaskType.REORDER_NEEDED)
                    .status(TaskStatus.ACKNOWLEDGED)
                    .brand(brand)
                    .message("Enum test")
                    .build();
            task = taskRepository.save(task);

            Task found = taskRepository.findById(task.getId()).orElseThrow();

            assertEquals(TaskType.REORDER_NEEDED, found.getTaskType());
            assertEquals(TaskStatus.ACKNOWLEDGED, found.getStatus());
        }
    }
}
