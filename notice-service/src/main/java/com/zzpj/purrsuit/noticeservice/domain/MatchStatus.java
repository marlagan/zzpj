package com.zzpj.purrsuit.noticeservice.domain;

/**
 * Status dopasowania (NoticeMatch) pomiędzy zgłoszeniem LOST a FOUND,
 * niezależny od statusu pet-service'owego MatchResult.
 */
public enum MatchStatus {
    PENDING,    // pet-service znalazł dopasowanie, czeka na decyzję użytkownika
    CONFIRMED,  // użytkownik potwierdził odnalezienie zwierzęcia
    REJECTED    // użytkownik odrzucił dopasowanie — wyszukiwanie jest wznawiane
}
