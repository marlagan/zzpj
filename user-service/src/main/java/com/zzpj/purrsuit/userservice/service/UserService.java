package com.zzpj.purrsuit.userservice.service;

import com.zzpj.purrsuit.userservice.dto.UserLoginDTO;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.entity.User;
import com.zzpj.purrsuit.userservice.enums.RoleName;
import com.zzpj.purrsuit.userservice.exceptions.*;
import com.zzpj.purrsuit.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserService {

    private UserRepository userRepository;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;

    public User getUserInfo(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoUserFoundException(
                        messageSource.getMessage("error.user.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));
    }

    public User getUserInfoByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoUserFoundException(
                        messageSource.getMessage("error.user.not.found", new Object[]{email}, LocaleContextHolder.getLocale())
                ));
    }

    public void deleteUser(UUID id) {

        if (!userRepository.existsById(id)){
            throw new NoUserFoundException(
                    messageSource.getMessage("error.user.not.found", new Object[]{id},
                            LocaleContextHolder.getLocale()));
        }

        userRepository.deleteById(id);
    }

    public void changeRole(UUID id, RoleName role) {

        if (!userRepository.existsById(id)){
            throw new NoUserFoundException(
                    messageSource.getMessage("error.user.not.found", new Object[]{id},
                            LocaleContextHolder.getLocale()));
        }

        User user = userRepository.findById(id).get();
        user.setRoleName(role);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String uploadUserImage(UUID id, MultipartFile file) {

        User user = getUserInfo(id);

        if (file.isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage("error.file.empty",
                            null, LocaleContextHolder.getLocale())
            );
        }

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/" + fileName;

            user.setImage(imageUrl);
            userRepository.save(user);

            return imageUrl;

        } catch (IOException e) {
            throw new FileStorageException(messageSource.getMessage(
                    "error.file.upload.failed", null, LocaleContextHolder.getLocale())
            );
        }
    }

    public User registerOrUpdateUser(UserRegistrationDTO dto) {
        Optional<User> existingUser = userRepository.findById(dto.getId());

        if (existingUser.isPresent()) {
            // Update existing user with fresh data from Keycloak
            User user = existingUser.get();
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            return userRepository.save(user);
        } else {
            // Create a new user since they don't exist yet
            User newUser = new User();
            newUser.setId(dto.getId()); // Use Keycloak ID as the internal ID
            newUser.setEmail(dto.getEmail());
            newUser.setFirstName(dto.getFirstName());
            newUser.setLastName(dto.getLastName());
            newUser.setRoleName(RoleName.USER);

            // Note: Password and phone number are likely not provided by Keycloak during sync
            // You might want to handle them accordingly or leave them empty

            return userRepository.save(newUser);
        }
    }

}
