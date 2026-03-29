package com.zzpj.purrsuit.userservice.repository;

import com.zzpj.purrsuit.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
