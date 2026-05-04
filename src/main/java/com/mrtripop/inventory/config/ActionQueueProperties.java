package com.mrtripop.inventory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.action-queue")
public class ActionQueueProperties {
    private int expiryWarningDays = 30;
    private String scanCron = "0 0 6 * * *";
    private boolean enabled = true;
}