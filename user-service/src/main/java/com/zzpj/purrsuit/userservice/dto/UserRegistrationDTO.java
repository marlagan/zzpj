package com.zzpj.purrsuit.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDTO {

    @NotBlank(message = "Provide firstName")
    private String firstName;

    @NotBlank(message = "Provide surName")
    private String lastName;

    @Email(message = "Incorrect email")
    private String email;

    @Pattern(regexp = "^\\d{9}$", message = "Incorrect phone number")
    private String phoneNumber;

    @Size(min = 8, message = "Password must have 8 characters")
    private String password;

}
