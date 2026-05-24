package com.zzpj.purrsuit.notificationservice.service;
import com.zzpj.purrsuit.notificationservice.dto.PetNotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "pet-service")
public interface PetServiceClient {

    @GetMapping("/notices/{id}")
    PetNotificationDTO getNotification(@PathVariable UUID id);
}