package com.mrtripop.inventory.scheduler;

import com.mrtripop.inventory.services.StockReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockReconciliationScheduler {
    private final StockReconciliationService reconciliationService;

    @Scheduled(cron = "${stock.reconciliation.cron:0 0 2 * * ?}")
    public void scheduledReconciliation() {
        log.info("Starting scheduled stock reconciliation...");
        reconciliationService.reconcileAll();
        log.info("Scheduled stock reconciliation completed.");
    }
}
