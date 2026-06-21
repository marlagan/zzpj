package com.zzpj.purrsuit.petservice.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status weryfikacji dopasowania ogłoszeń")
public enum MatchStatus {

    @Schema(description = "W trakcie przetwarzania")
    PENDING,

    @Schema(description = "Dopasowanie potwierdzone przez użytkownika")
    CONFIRMED,

    @Schema(description = "Dopasowanie odrzucone przez użytkownika")
    REJECTED
}
