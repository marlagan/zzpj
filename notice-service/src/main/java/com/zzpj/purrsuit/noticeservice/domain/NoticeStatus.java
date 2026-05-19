package com.zzpj.purrsuit.noticeservice.domain;

public enum NoticeStatus {
    ACTIVE,             // Ogłoszenie widoczne, szukamy
    PENDING_MATCH,      // Algorytm znalazł dopasowanie, czeka na potwierdzenie użytkowników
    RESOLVED,           // Zwierzę odnalezione / oddane właścicielowi
    CLOSED              // Zamknięte z innych powodów (np. pomyłka, przedawnienie)
}