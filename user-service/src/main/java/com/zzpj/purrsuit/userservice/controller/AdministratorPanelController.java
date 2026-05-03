package com.zzpj.purrsuit.userservice.controller;

import com.zzpj.purrsuit.userservice.dto.UserCreationDTO;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.entity.User;
import com.zzpj.purrsuit.userservice.enums.RoleName;
import com.zzpj.purrsuit.userservice.exceptions.*;
import com.zzpj.purrsuit.userservice.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:3000")
public class AdministratorPanelController {

    private UserService userService;

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserCreationDTO dto) {

        try {
            userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (EmailAlreadyRegisteredException e) {
            log.error("Create user error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");

        } catch (NoUserFoundException e) {
            log.error("Delete user error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            log.error("Error while deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<String> changeRole(@PathVariable Long id, @RequestParam RoleName role) {

        try {
            userService.changeRole(id, role);
            return ResponseEntity.ok("Role updated successfully");

        } catch (NoSuchRoleException e) {
            log.error("No such a role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (NoUserFoundException e) {
            log.error("No such a user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            log.error("Fetch users error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserInfo(id);
            return ResponseEntity.ok(user);

        } catch (NoUserFoundException e) {
            log.error("Get user error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
