package com.zzpj.purrsuit.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetNotificationDTO {
    private UUID id;
    private String species;
    private String description;
    private String type;
}
