package com.zzpj.purrsuit.notificationservice.repository;

import com.zzpj.purrsuit.notificationservice.entity.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, UUID> {
}
