package com.zzpj.purrsuit.petservice.dto;

import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import java.util.UUID;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Obiekt reprezentujący wynik semantycznego dopasowania dwóch ogłoszeń")
public record MatchResultDto(
        @Schema(description = "Unikalny identyfikator wyniku dopasowania w bazie danych")
        UUID id,

        @Schema(description = "Identyfikator ogłoszenia o zgubionym zwierzęciu")
        UUID lostNoticeId,

        @Schema(description = "Identyfikator ogłoszenia o znalezionym zwierzęciu")
        UUID seenNoticeId,

        @Schema(description = "Identyfikator użytkownika, który wystawił ogłoszenie o zgubieniu")
        UUID lostOwnerId,

        @Schema(description = "Współczynnik prawdopodobieństwa (0.0 - 1.0) określający zbieżność opisów wyliczoną przez model AI")
        double similarityScore,

        @Schema(description = "Aktualny status procesu weryfikacji dopasowania")
        MatchStatus status,

        @Schema(description = "Data i czas utworzenia rekordu dopasowania w systemie")
        LocalDateTime createdAt
) {}
