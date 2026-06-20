package com.zzpj.purrsuit.userservice.controller;

import com.zzpj.purrsuit.userservice.dto.PasswordChangeDTO;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.entity.User;
import com.zzpj.purrsuit.userservice.enums.RoleName;
import com.zzpj.purrsuit.userservice.service.KeycloakAuthService;
import com.zzpj.purrsuit.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final KeycloakAuthService keycloakAuthService;

    @PostMapping("/sync-profile")
    public ResponseEntity<User> syncProfileWithKeycloak(@AuthenticationPrincipal Jwt jwt) {
        UUID keycloakId = UUID.fromString(jwt.getClaimAsString("sub"));
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        RoleName role = keycloakAuthService.resolveRoleFromToken(jwt);

        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setId(keycloakId);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setRoleName(role);

        User user = userService.registerOrUpdateUser(dto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PasswordChangeDTO dto
    ) {
        try {
            UUID keycloakId = UUID.fromString(jwt.getClaimAsString("sub"));
            String email = jwt.getClaimAsString("email");
            keycloakAuthService.changeUserPassword(
                    keycloakId.toString(),
                    email,
                    dto.getOldPassword(),
                    dto.getNewPassword()
            );
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
