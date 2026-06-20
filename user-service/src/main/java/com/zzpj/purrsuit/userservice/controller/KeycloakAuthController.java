package com.zzpj.purrsuit.userservice.controller;

import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.service.KeycloakAuthService;
import com.zzpj.purrsuit.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/public")
@RequiredArgsConstructor
public class KeycloakAuthController {

    private final KeycloakAuthService keycloakAuthService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDTO dto) {
        UUID keycloakId = keycloakAuthService.registerUser(dto);

        dto.setId(keycloakId);
        userService.registerOrUpdateUser(dto);

        return ResponseEntity.ok("User registered successfully");
    }
}
