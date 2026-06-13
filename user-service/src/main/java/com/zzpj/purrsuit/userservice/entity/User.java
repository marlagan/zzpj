package com.zzpj.purrsuit.userservice.entity;

import com.zzpj.purrsuit.userservice.enums.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    private String firstName;

    private String lastName;

    @Email(message = "Bad email")
    // Dodano unique = true. Mail musi być unikalny, żeby nie było duplikatów.
    @Column(nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleName roleName;

    private String image; // Zostawiamy jako String (link lub nazwa pliku z dysku/S3)
}