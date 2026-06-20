package com.zzpj.purrsuit.notificationservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "http://user-service:8086")
public interface UserServiceClient {

    @GetMapping("/users/{id}/email")
    String getUserEmail(@PathVariable UUID id);
}