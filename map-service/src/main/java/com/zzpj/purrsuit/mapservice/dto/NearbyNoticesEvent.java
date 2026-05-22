package com.zzpj.purrsuit.mapservice.dto; // W pet-service zmień pakiet

import java.util.List;
import java.util.UUID;

public record NearbyNoticesEvent(
    UUID lostNoticeId,            // ID zgubionego zwierzaka
    List<UUID> nearbyFoundNoticeIds // Lista ID znalezionych zwierzaków w okolicy
) {}