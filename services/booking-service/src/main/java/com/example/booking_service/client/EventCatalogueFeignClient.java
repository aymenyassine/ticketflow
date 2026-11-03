package com.example.booking_service.client;

import com.example.booking_service.client.dto.EventView;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", contextId = "eventCatalogueClient")
public interface EventCatalogueFeignClient {
    @GetMapping("/api/v1/events/{id}")
    EventView getPublished(@PathVariable("id") UUID id);
}
