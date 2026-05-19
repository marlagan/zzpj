package com.zzpj.purrsuit.userservice.controller;

import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.entity.User;
import com.zzpj.purrsuit.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * Endpoint wywoływany przez Frontend natychmiast po udanym logowaniu w Keycloak.
     * Adnotacja @AuthenticationPrincipal Jwt automatycznie "wyciąga" token z nagłówka żądania.
     */
    @PostMapping("/sync-profile")
    public ResponseEntity<User> syncProfileWithKeycloak(@AuthenticationPrincipal Jwt jwt) {

        // 1. Wyciągamy dane z tokena JWT (Keycloak automatycznie zaszywa tam te informacje)
        UUID keycloakId = UUID.fromString(jwt.getClaimAsString("sub"));
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        // 2. Pakujemy je w DTO
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setId(keycloakId);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);

        // 3. Wywołujemy metodę w UserService (tę z "Wielkiego Sprzątania"),
        // która sprawdzi czy user już u nas jest, a jeśli nie - utworzy go.
        User user = userService.registerOrUpdateUser(dto);

        return ResponseEntity.ok(user);
    }
}