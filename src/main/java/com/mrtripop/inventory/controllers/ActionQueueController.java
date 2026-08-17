package com.mrtripop.inventory.controllers;

import com.mrtripop.inventory.constant.SuccessCode;
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
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TaskDto> result = actionQueueService.getTasks(storeId, status, pageable);
        return ResponseBody.builder()
                .code(SuccessCode.INV2012_GET_TASKS_SUCCESS.getCode())
                .message(SuccessCode.INV2012_GET_TASKS_SUCCESS.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable Long id) {
        TaskDto result = actionQueueService.getTaskById(id);
        return ResponseBody.builder()
                .code(SuccessCode.INV2013_GET_TASK_BY_ID_SUCCESS.getCode())
                .message(SuccessCode.INV2013_GET_TASK_BY_ID_SUCCESS.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/tasks/{id}/acknowledge")
    public ResponseEntity<Object> acknowledgeTask(@PathVariable Long id) {
        TaskDto result = actionQueueService.acknowledgeTask(id);
        return ResponseBody.builder()
                .code(SuccessCode.INV2014_ACKNOWLEDGE_TASK_SUCCESS.getCode())
                .message(SuccessCode.INV2014_ACKNOWLEDGE_TASK_SUCCESS.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/tasks/{id}/resolve")
    public ResponseEntity<Object> resolveTask(@PathVariable Long id) {
        TaskDto result = actionQueueService.resolveTask(id);
        return ResponseBody.builder()
                .code(SuccessCode.INV2015_RESOLVE_TASK_SUCCESS.getCode())
                .message(SuccessCode.INV2015_RESOLVE_TASK_SUCCESS.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/tasks/scan")
    public ResponseEntity<Object> triggerScan() {
        ActionQueueScanResult result = actionQueueService.runFullScan();
        return ResponseBody.builder()
                .code(SuccessCode.INV2016_SCAN_TRIGGERED_SUCCESS.getCode())
                .message(SuccessCode.INV2016_SCAN_TRIGGERED_SUCCESS.getMessage())
                .data(result)
                .build()
                .toResponseEntity(HttpStatus.OK);
    }
}