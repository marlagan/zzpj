package com.zzpj.purrsuit.petservice.repository;
import com.zzpj.purrsuit.petservice.entity.PetNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Interfejs dostępu do bazy danych dla replikowanych ogłoszeń o zwierzętach.
 */
public interface PetNoticeRepository extends JpaRepository<PetNotice, UUID> {

    /**
     * Wyszukuje ogłoszenia o zadanym typie i gatunku.
     *
     * @param type typ ogłoszenia (LOST/FOUND)
     * @param species gatunek zwierzęcia
     *
     * @return lista pasujących ogłoszeń
     */
    List<PetNotice> findByTypeAndSpecies(String type, String species);
}
