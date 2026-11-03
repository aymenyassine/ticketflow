package com.example.booking_service.client;

import com.example.booking_service.client.dto.InventoryHoldRequest;
import com.example.booking_service.client.dto.InventoryHoldView;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "event-service", contextId = "eventInventoryClient")
public interface EventInventoryFeignClient {
    @PutMapping("/internal/v1/inventory/holds/{id}")
    InventoryHoldView hold(@PathVariable("id") UUID id, @RequestBody InventoryHoldRequest request);

    @PostMapping("/internal/v1/inventory/holds/{id}/confirm")
    InventoryHoldView confirm(@PathVariable("id") UUID id);

    @DeleteMapping("/internal/v1/inventory/holds/{id}")
    InventoryHoldView release(@PathVariable("id") UUID id);
}
