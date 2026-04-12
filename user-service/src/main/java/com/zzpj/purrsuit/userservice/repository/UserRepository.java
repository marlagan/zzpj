package com.zzpj.purrsuit.userservice.repository;

import com.zzpj.purrsuit.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByFirstName(String name);
    List<User> findByLastName(String lastName);
    List<User> findByEmail(String email);
    List<User> findByPhoneNumber(String phoneNumber);


}
