package com.mrtripop.inventory.fixture;

import com.mrtripop.clinical.models.db.Brand;
import com.mrtripop.clinical.models.db.Store;
import com.mrtripop.inventory.models.db.*;
import com.mrtripop.inventory.models.dto.TaskDto;
import java.util.List;
import java.util.UUID;

public final class TaskFixture {

    private TaskFixture() {}

    public static final List<TaskStatus> ACTIVE_STATUSES = List.of(TaskStatus.PENDING, TaskStatus.ACKNOWLEDGED);

    public static Task.TaskBuilder<?, ?> pendingExpiryWarningTask(Store store, Batch batch, Brand brand) {
        return Task.builder()
                .store(store)
                .taskType(TaskType.EXPIRY_WARNING)
                .status(TaskStatus.PENDING)
                .batch(batch)
                .brand(brand)
                .message("Test expiry warning")
                .daysUntilExpiry(15);
    }

    public static Task.TaskBuilder<?, ?> pendingReorderNeededTask(Store store, Brand brand, long currentQty, long threshold) {
        return Task.builder()
                .store(store)
                .taskType(TaskType.REORDER_NEEDED)
                .status(TaskStatus.PENDING)
                .brand(brand)
                .message("Test reorder needed")
                .currentQuantity(currentQty)
                .thresholdQuantity(threshold);
    }

    public static Task.TaskBuilder<?, ?> acknowledgedExpiryWarningTask(Store store, Batch batch, Brand brand) {
        return Task.builder()
                .store(store)
                .taskType(TaskType.EXPIRY_WARNING)
                .status(TaskStatus.ACKNOWLEDGED)
                .batch(batch)
                .brand(brand)
                .message("Acknowledged expiry warning")
                .daysUntilExpiry(15);
    }

    public static Task.TaskBuilder<?, ?> acknowledgedTask(Store store, Brand brand) {
        return Task.builder()
                .store(store)
                .taskType(TaskType.EXPIRY_WARNING)
                .status(TaskStatus.ACKNOWLEDGED)
                .brand(brand)
                .message("Acknowledged task");
    }

    public static Task.TaskBuilder<?, ?> resolvedTask(Store store, Brand brand) {
        return Task.builder()
                .store(store)
                .taskType(TaskType.EXPIRY_WARNING)
                .status(TaskStatus.RESOLVED)
                .brand(brand)
                .message("Resolved task");
    }

    public static TaskDto defaultTaskDto() {
        return TaskDto.builder()
                .id(1L)
                .storeId(UUID.randomUUID())
                .storeName("Test Store")
                .taskType(TaskType.EXPIRY_WARNING.name())
                .status(TaskStatus.PENDING.name())
                .message("Test task message")
                .build();
    }
}
