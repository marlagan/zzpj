package com.zzpj.purrsuit.petservice.repository;
import com.zzpj.purrsuit.petservice.entity.PetNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PetNoticeRepository extends JpaRepository<PetNotice, UUID> {
    List<PetNotice> findByTypeAndSpecies(String type, String species);
}
