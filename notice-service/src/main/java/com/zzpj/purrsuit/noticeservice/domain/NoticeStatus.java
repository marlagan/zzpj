package com.zzpj.purrsuit.noticeservice.domain;

public enum NoticeStatus {
    PENDING_AI_REVIEW,  // Czeka na potwierdzenie opisu AI przez użytkownika
    ACTIVE,             // Widoczne, szukamy — po confirm-description
    PENDING_MATCH,      // Algorytm znalazł dopasowanie, czeka na potwierdzenie
    RESOLVED,           // Zwierzę odnalezione
    CLOSED              // Zamknięte (pomyłka, przedawnienie)
}
