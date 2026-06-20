package com.zzpj.purrsuit.noticeservice.domain;

public enum NoticeStatus {
    ACTIVE,         // Ogłoszenie widoczne, szukamy
    PENDING_MATCH,  // Algorytm znalazł dopasowanie, czeka na potwierdzenie
    RESOLVED,       // Zwierzę odnalezione
    CLOSED          // Zamknięte
}
