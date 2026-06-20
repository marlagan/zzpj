package com.zzpj.purrsuit.userservice.service;

import com.zzpj.purrsuit.userservice.enums.RoleName;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class KeycloakAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.server-url:http://keycloak:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:purrsuit-realm}")
    private String realm;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    @Value("${keycloak.client-id:purrsuit-client}")
    private String clientId;

    public RoleName resolveRoleFromToken(org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (hasAdminRole(jwt.getClaim("resource_access"))) {
            return RoleName.ADMIN;
        }
        if (hasAdminRole(jwt.getClaim("realm_access"))) {
            return RoleName.ADMIN;
        }
        return RoleName.USER;
    }

    @SuppressWarnings("unchecked")
    private boolean hasAdminRole(Object accessClaim) {
        if (!(accessClaim instanceof Map<?, ?> access)) {
            return false;
        }
        Object clientAccess = access.get(clientId);
        if (clientAccess instanceof Map<?, ?> client && client.get("roles") instanceof java.util.List<?> roles) {
            return roles.stream().anyMatch("ADMIN"::equals);
        }
        if (access.get("roles") instanceof java.util.List<?> roles) {
            return roles.stream().anyMatch("ADMIN"::equals);
        }
        return false;
    }

    public void changeUserPassword(String keycloakUserId, String email, String oldPassword, String newPassword) {
        verifyCurrentPassword(email, oldPassword);
        setUserPassword(getAdminToken(), keycloakUserId, newPassword);
    }

    private void verifyCurrentPassword(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("username", email);
        body.add("password", password);

        try {
            restTemplate.exchange(
                    serverUrl + "/realms/" + realm + "/protocol/openid-connect/token",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("Incorrect current password");
        }
    }

    public UUID registerUser(UserRegistrationDTO dto) {
        String adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> payload = Map.of(
                "username", dto.getEmail(),
                "email", dto.getEmail(),
                "firstName", dto.getFirstName(),
                "lastName", dto.getLastName(),
                "enabled", true,
                "emailVerified", true
        );

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    serverUrl + "/admin/realms/" + realm + "/users",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    Void.class
            );

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new IllegalStateException("Keycloak did not return created user location");
            }

            String keycloakUserId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
            setUserPassword(adminToken, keycloakUserId, dto.getPassword());
            return UUID.fromString(keycloakUserId);
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }

    private String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        ResponseEntity<Map> response = restTemplate.exchange(
                serverUrl + "/realms/master/protocol/openid-connect/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Object token = response.getBody() != null ? response.getBody().get("access_token") : null;
        if (token == null) {
            throw new IllegalStateException("Failed to obtain Keycloak admin token");
        }
        return token.toString();
    }

    private void setUserPassword(String adminToken, String keycloakUserId, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> payload = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );

        restTemplate.exchange(
                serverUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password",
                HttpMethod.PUT,
                new HttpEntity<>(payload, headers),
                Void.class
        );
    }
}
