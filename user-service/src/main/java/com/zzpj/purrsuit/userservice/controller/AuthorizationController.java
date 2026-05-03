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

@AllArgsConstructor
@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AuthorizationController {

    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDTO userLoginDTO){
        String token = "";
        try {
            token = userService.loginUser(userLoginDTO);
        }catch (IncorrectPasswordException | EmailDoesNotExistException e){
            log.error("Login error: ", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO){

        try {
            userService.registerUser(userRegistrationDTO);
        }catch (EmailAlreadyRegisteredException | PhoneNumberAlreadyRegisteredException e){
            log.error("Register error: ", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }

        return ResponseEntity.ok("Registration was successful");
    }

    @PatchMapping("/change-password/{id}")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO, @PathVariable Long id){

        try {
            userService.changePassword(id, passwordChangeDTO.getNewPassword(), passwordChangeDTO.getOldPassword());
        }catch (EmailAlreadyRegisteredException | PhoneNumberAlreadyRegisteredException e){
            log.error("Password change failure: ", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok("Registration was successful");
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
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