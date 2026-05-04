package com.mrtripop.inventory.services.impl;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.StoreProduct;
import com.mrtripop.clinical.repository.StoreProductRepository;
import com.mrtripop.clinical.services.AuditService;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.component.TaskMapper;
import com.mrtripop.inventory.config.ActionQueueProperties;
import com.mrtripop.inventory.constant.ErrorCode;
import com.mrtripop.inventory.models.db.*;
import com.mrtripop.inventory.models.dto.ActionQueueScanResult;
import com.mrtripop.inventory.models.dto.TaskDto;
import com.mrtripop.inventory.repository.StoreStockRepository;
import com.mrtripop.inventory.repository.TaskRepository;
import com.mrtripop.inventory.services.ActionQueueService;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
public class ActionQueueServiceImpl implements ActionQueueService {

    private final TaskRepository taskRepository;
    private final StoreStockRepository storeStockRepository;
    private final StoreProductRepository storeProductRepository;
    private final AuditService auditService;
    private final TaskMapper taskMapper;
    private final ActionQueueProperties properties;

    private static final List<TaskStatus> ACTIVE_STATUSES = List.of(TaskStatus.PENDING, TaskStatus.ACKNOWLEDGED);

    @Override
    public Page<TaskDto> getTasks(UUID storeId, TaskStatus status, Pageable pageable) {
        if (status != null) {
            return taskRepository.findByStore_IdAndStatus(storeId, status, pageable).map(taskMapper::toDto);
        }
        return taskRepository.findByStore_Id(storeId, pageable).map(taskMapper::toDto);
    }

    @Override
    public TaskDto getTaskById(Long id) throws ApplicationException {
        Task task = taskRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional(rollbackFor = ApplicationException.class)
    public TaskDto acknowledgeTask(Long id) throws ApplicationException {
        Task task = taskRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (task.getStatus() == TaskStatus.RESOLVED) {
            throw new ApplicationException(ErrorCode.TASK_ALREADY_RESOLVED, HttpStatus.CONFLICT);
        }
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new ApplicationException(ErrorCode.INVALID_TASK_STATUS, HttpStatus.CONFLICT);
        }
        task.setStatus(TaskStatus.ACKNOWLEDGED);
        task = saveTaskOrThrowConflict(task);
        auditService.recordAudit("ACTION_QUEUE_ACKNOWLEDGED", "Task", task.getId().toString(),
                TaskStatus.PENDING.name(), TaskStatus.ACKNOWLEDGED.name());
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional(rollbackFor = ApplicationException.class)
    public TaskDto resolveTask(Long id) throws ApplicationException {
        Task task = taskRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (task.getStatus() == TaskStatus.RESOLVED) {
            throw new ApplicationException(ErrorCode.TASK_ALREADY_RESOLVED, HttpStatus.CONFLICT);
        }
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.RESOLVED);
        task = saveTaskOrThrowConflict(task);
        auditService.recordAudit("ACTION_QUEUE_RESOLVED", "Task", task.getId().toString(),
                oldStatus.name(), TaskStatus.RESOLVED.name());
        return taskMapper.toDto(task);
    }

    // Per-task transaction: each save runs in its own transaction via auto-commit.
    // @Transactional is intentionally NOT on scan methods because self-invocation
    // from runFullScan() bypasses the Spring proxy, making them ineffective.
    @Override
    public ActionQueueScanResult runExpiryScan() {
        int created = 0;
        int updated = 0;
        List<UUID> storeIds = storeStockRepository.findDistinctStoreIds();
        LocalDate thresholdDate = LocalDate.now().plusDays(properties.getExpiryWarningDays());

        for (UUID storeId : storeIds) {
            List<StoreStock> expiringStock = storeStockRepository.findExpiringSoonByStore(storeId, thresholdDate);
            for (StoreStock stock : expiringStock) {
                Batch batch = stock.getBatch();
                Brand brand = batch.getBrand();
                int daysUntilExpiry = (int) ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
                String message = String.format("Batch %s of %s expires in %d days (%s)",
                        batch.getBatchNumber(), brand.getBrandName(), daysUntilExpiry, batch.getExpiryDate());

                var existing = taskRepository.findActiveExpiryTask(storeId, batch.getId(), TaskType.EXPIRY_WARNING, ACTIVE_STATUSES);
                if (existing.isPresent()) {
                    Task task = existing.get();
                    task.setDaysUntilExpiry(daysUntilExpiry);
                    task.setMessage(message);
                    taskRepository.save(task);
                    auditService.recordAudit("ACTION_QUEUE_UPDATED", "Task", task.getId().toString(),
                            "daysUntilExpiry", String.valueOf(daysUntilExpiry));
                    updated++;
                } else {
                    Task task = Task.builder()
                            .store(stock.getStore())
                            .taskType(TaskType.EXPIRY_WARNING)
                            .status(TaskStatus.PENDING)
                            .batch(batch)
                            .brand(brand)
                            .message(message)
                            .daysUntilExpiry(daysUntilExpiry)
                            .build();
                    taskRepository.save(task);
                    auditService.recordAudit("ACTION_QUEUE_CREATED", "Task", task.getId().toString(),
                            null, message);
                    created++;
                }
            }
        }
        return ActionQueueScanResult.builder()
                .expiryWarningsCreated(created)
                .expiryWarningsUpdated(updated)
                .build();
    }

    @Override
    public ActionQueueScanResult runReorderScan() {
        int created = 0;
        int updated = 0;
        List<StoreProduct> storeProducts = storeProductRepository.findWithReorderThreshold();

        for (StoreProduct sp : storeProducts) {
            UUID storeId = sp.getStore().getId();
            UUID brandId = sp.getBrand().getId();
            Long available = storeStockRepository.sumAvailableQuantityByStoreAndBrand(storeId, brandId);

            if (available < sp.getReorderThreshold()) {
                String message = String.format("Stock for %s is low: %d units remaining (threshold: %d)",
                        sp.getBrand().getBrandName(), available, sp.getReorderThreshold());

                var existing = taskRepository.findActiveReorderTask(storeId, brandId, TaskType.REORDER_NEEDED, ACTIVE_STATUSES);
                if (existing.isPresent()) {
                    Task task = existing.get();
                    task.setCurrentQuantity(available);
                    task.setMessage(message);
                    taskRepository.save(task);
                    auditService.recordAudit("ACTION_QUEUE_UPDATED", "Task", task.getId().toString(),
                            "currentQuantity", String.valueOf(available));
                    updated++;
                } else {
                    Task task = Task.builder()
                            .store(sp.getStore())
                            .taskType(TaskType.REORDER_NEEDED)
                            .status(TaskStatus.PENDING)
                            .brand(sp.getBrand())
                            .message(message)
                            .currentQuantity(available)
                            .thresholdQuantity(sp.getReorderThreshold())
                            .build();
                    taskRepository.save(task);
                    auditService.recordAudit("ACTION_QUEUE_CREATED", "Task", task.getId().toString(),
                            null, message);
                    created++;
                }
            }
        }
        return ActionQueueScanResult.builder()
                .reorderAlertsCreated(created)
                .reorderAlertsUpdated(updated)
                .build();
    }

    @Override
    public ActionQueueScanResult runFullScan() {
        ActionQueueScanResult expiryResult = runExpiryScan();
        ActionQueueScanResult reorderResult = runReorderScan();
        return ActionQueueScanResult.builder()
                .expiryWarningsCreated(expiryResult.getExpiryWarningsCreated())
                .expiryWarningsUpdated(expiryResult.getExpiryWarningsUpdated())
                .reorderAlertsCreated(reorderResult.getReorderAlertsCreated())
                .reorderAlertsUpdated(reorderResult.getReorderAlertsUpdated())
                .build();
    }

    private Task saveTaskOrThrowConflict(Task task) throws ApplicationException {
        try {
            return taskRepository.save(task);
        } catch (OptimisticLockException e) {
            throw new ApplicationException(ErrorCode.TASK_ALREADY_RESOLVED, HttpStatus.CONFLICT);
        }
    }
}
