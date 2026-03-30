package com.zzpj.purrsuit.userservice.controller;

import com.zzpj.purrsuit.userservice.dto.UserLoginDTO;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.exceptions.EmailAlreadyRegisteredException;
import com.zzpj.purrsuit.userservice.exceptions.EmailDoesNotExistException;
import com.zzpj.purrsuit.userservice.exceptions.IncorrectPasswordException;
import com.zzpj.purrsuit.userservice.exceptions.PhoneNumberAlreadyRegisteredException;
import com.zzpj.purrsuit.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@RestController
public class AuthorizationController {

    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDTO userLoginDTO){
        try {
            userService.loginUser(userLoginDTO);
        }catch (IncorrectPasswordException | EmailDoesNotExistException e){
            log.error("Login error: ", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        return ResponseEntity.ok("Login was successful");
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

}
