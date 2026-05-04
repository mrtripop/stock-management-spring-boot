package com.mrtripop.inventory.engine;

import static org.mockito.Mockito.*;

import com.mrtripop.inventory.config.ActionQueueProperties;
import com.mrtripop.inventory.models.dto.ActionQueueScanResult;
import com.mrtripop.inventory.services.ActionQueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActionQueueEngine")
class ActionQueueEngineTest {

    @Mock private ActionQueueService actionQueueService;
    @Mock private ActionQueueProperties properties;
    @InjectMocks private ActionQueueEngine actionQueueEngine;

    @Test
    @DisplayName("should delegate to service when scan is enabled")
    void runScheduledScan_enabled_delegatesToService() {
        when(properties.isEnabled()).thenReturn(true);
        ActionQueueScanResult result = ActionQueueScanResult.builder()
                .expiryWarningsCreated(1).expiryWarningsUpdated(0)
                .reorderAlertsCreated(2).reorderAlertsUpdated(0).build();
        when(actionQueueService.runFullScan()).thenReturn(result);

        actionQueueEngine.runScheduledScan();

        verify(actionQueueService).runFullScan();
    }

    @Test
    @DisplayName("should skip scan when disabled")
    void runScheduledScan_disabled_skipsScan() {
        when(properties.isEnabled()).thenReturn(false);

        actionQueueEngine.runScheduledScan();

        verify(actionQueueService, never()).runFullScan();
    }
}
