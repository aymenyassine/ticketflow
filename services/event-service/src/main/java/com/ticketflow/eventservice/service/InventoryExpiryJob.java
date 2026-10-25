package com.ticketflow.eventservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryExpiryJob {
    private final InventoryService inventory;
    public InventoryExpiryJob(InventoryService inventory) { this.inventory = inventory; }
    @Scheduled(fixedDelayString = "${event.inventory.expiry-check-ms:60000}")
    public void releaseExpired() { inventory.releaseExpired(); }
}
