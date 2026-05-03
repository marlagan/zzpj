package com.zzpj.purrsuit.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeDTO {
    @NotBlank(message = "Provide new password")
    @Size(min = 8, message = "Password must have 8 characters")
    private String newPassword;
    @NotBlank(message = "Provide old password")
    private String oldPassword;
}
