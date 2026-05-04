package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.Task;
import com.mrtripop.inventory.models.db.TaskStatus;
import com.mrtripop.inventory.models.db.TaskType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"store", "batch", "brand"})
    @Query("SELECT t FROM Task t WHERE t.id = :id")
    Optional<Task> findWithDetailsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"store", "batch", "brand"})
    Page<Task> findByStore_IdAndStatus(UUID storeId, TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"store", "batch", "brand"})
    Page<Task> findByStore_Id(UUID storeId, Pageable pageable);

    @Query("SELECT t FROM Task t "
        + "WHERE t.store.id = :storeId AND t.batch.id = :batchId "
        + "AND t.taskType = :taskType AND t.status IN :activeStatuses")
    Optional<Task> findActiveExpiryTask(
        @Param("storeId") UUID storeId,
        @Param("batchId") Long batchId,
        @Param("taskType") TaskType taskType,
        @Param("activeStatuses") List<TaskStatus> activeStatuses);

    @Query("SELECT t FROM Task t "
        + "WHERE t.store.id = :storeId AND t.brand.id = :brandId "
        + "AND t.taskType = :taskType AND t.status IN :activeStatuses")
    Optional<Task> findActiveReorderTask(
        @Param("storeId") UUID storeId,
        @Param("brandId") UUID brandId,
        @Param("taskType") TaskType taskType,
        @Param("activeStatuses") List<TaskStatus> activeStatuses);
}