package com.mrtripop.inventory.engine;

import com.mrtripop.inventory.config.ActionQueueProperties;
import com.mrtripop.inventory.services.ActionQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionQueueEngine {

    private final ActionQueueService actionQueueService;
    private final ActionQueueProperties properties;

    @Scheduled(cron = "${app.action-queue.scan-cron:0 0 6 * * *}")
    public void runScheduledScan() {
        if (!properties.isEnabled()) {
            log.debug("Action queue scan is disabled, skipping");
            return;
        }
        log.info("Starting scheduled action queue scan");
        try {
            var result = actionQueueService.runFullScan();
            log.info("Action queue scan completed: {} expiry warnings created, {} updated, {} reorder alerts created, {} updated",
                    result.getExpiryWarningsCreated(), result.getExpiryWarningsUpdated(),
                    result.getReorderAlertsCreated(), result.getReorderAlertsUpdated());
        } catch (Exception e) {
            log.error("Action queue scan failed", e);
        }
    }
}