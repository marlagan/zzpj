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

    public User registerOrUpdateUser(UserRegistrationDTO dto) {
        Optional<User> existingUser = userRepository.findById(dto.getId());

        if (existingUser.isPresent()) {
            // Update existing user with fresh data from Keycloak
            User user = existingUser.get();
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
                user.setPhoneNumber(dto.getPhoneNumber());
            }
            if (dto.getRoleName() != null) {
                user.setRoleName(dto.getRoleName());
            }
            return userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setId(dto.getId());
            newUser.setEmail(dto.getEmail());
            newUser.setFirstName(dto.getFirstName());
            newUser.setLastName(dto.getLastName());
            newUser.setPhoneNumber(dto.getPhoneNumber());
            newUser.setRoleName(dto.getRoleName() != null ? dto.getRoleName() : RoleName.USER);
            return userRepository.save(newUser);
        }
    }

}
