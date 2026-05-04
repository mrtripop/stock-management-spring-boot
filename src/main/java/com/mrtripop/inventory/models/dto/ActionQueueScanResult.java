package com.mrtripop.inventory.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionQueueScanResult {
    private int expiryWarningsCreated;
    private int expiryWarningsUpdated;
    private int reorderAlertsCreated;
    private int reorderAlertsUpdated;
}