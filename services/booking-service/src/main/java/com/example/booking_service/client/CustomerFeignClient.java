package com.example.booking_service.client;

import com.example.booking_service.client.dto.CustomerView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerFeignClient {
    @GetMapping("/api/customers/{id}")
    CustomerView get(@PathVariable("id") Long id);
}
