package com.mrtripop.inventory.controllers;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.inventory.models.dto.ActionQueueScanResult;
import com.mrtripop.inventory.models.dto.TaskDto;
import com.mrtripop.inventory.models.db.TaskStatus;
import com.mrtripop.inventory.services.ActionQueueService;
import com.mrtripop.model.ResponseBody;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
@Validated
public class ActionQueueController {

    private final ActionQueueService actionQueueService;

    @GetMapping("/tasks")
    public ResponseEntity<Object> getTasks(
            @RequestParam UUID storeId,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) throws ApplicationException {
        Page<TaskDto> result = actionQueueService.getTasks(storeId, status, pageable);
        return ResponseBody.builder()
                .code("INV2001")
                .message("Tasks retrieved successfully")
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable Long id) throws ApplicationException {
        TaskDto result = actionQueueService.getTaskById(id);
        return ResponseBody.builder()
                .code("INV2001")
                .message("Task retrieved successfully")
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/tasks/{id}/acknowledge")
    public ResponseEntity<Object> acknowledgeTask(@PathVariable Long id) throws ApplicationException {
        TaskDto result = actionQueueService.acknowledgeTask(id);
        return ResponseBody.builder()
                .code("INV2001")
                .message("Task acknowledged successfully")
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/tasks/{id}/resolve")
    public ResponseEntity<Object> resolveTask(@PathVariable Long id) throws ApplicationException {
        TaskDto result = actionQueueService.resolveTask(id);
        return ResponseBody.builder()
                .code("INV2001")
                .message("Task resolved successfully")
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/tasks/scan")
    public ResponseEntity<Object> triggerScan() throws ApplicationException {
        ActionQueueScanResult result = actionQueueService.runFullScan();
        return ResponseBody.builder()
                .code("INV2001")
                .message("Scan completed successfully")
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }
}