package com.zzpj.purrsuit.userservice.entity;

import com.zzpj.purrsuit.userservice.enums.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Email(message = "Bad email")
    private String email;
    //I have to add pattern and regex later
    private String phoneNumber;
    @Size(message = "min 8 characters")
    //regex later
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

}
