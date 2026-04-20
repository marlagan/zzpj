package com.zzpj.purrsuit.perservice.repository;

import com.zzpj.purrsuit.perservice.model.NoticeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NoticeEmbeddingRepository extends JpaRepository<NoticeEmbedding,UUID>{
}
