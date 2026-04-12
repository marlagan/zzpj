package com.zzpj.purrsuit.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {

    @NotBlank(message = "Provide email")
    private String email;
    @NotBlank(message = "Provide password")
    private String password;

}
