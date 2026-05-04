package com.mrtripop.inventory.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.ActionQueueScanResult;
import com.mrtripop.inventory.models.dto.TaskDto;
import com.mrtripop.inventory.models.db.TaskStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActionQueueService {
    Page<TaskDto> getTasks(UUID storeId, TaskStatus status, Pageable pageable);
    TaskDto getTaskById(Long id) throws ApplicationException;
    TaskDto acknowledgeTask(Long id) throws ApplicationException;
    TaskDto resolveTask(Long id) throws ApplicationException;
    ActionQueueScanResult runExpiryScan();
    ActionQueueScanResult runReorderScan();
    ActionQueueScanResult runFullScan();
}