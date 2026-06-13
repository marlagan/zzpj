package com.zzpj.purrsuit.userservice.controller;

import com.zzpj.purrsuit.userservice.dto.PasswordChangeDTO;
import com.zzpj.purrsuit.userservice.dto.UserLoginDTO;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.exceptions.*;
import com.zzpj.purrsuit.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthorizationController {

    private UserService userService;

    @PostMapping("/upload/{id}") // todo move to profile
    public ResponseEntity<?> uploadImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        try {
            String imagePath = userService.uploadUserImage(id, file);
            return ResponseEntity.ok(imagePath);

        } catch (FileStorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());

        } catch (NoUserFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }

}