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
    private final JwtService jwtService;

    public void registerUser(UserRegistrationDTO userRegistrationDTO){

        String email = userRegistrationDTO.getEmail();
        String phoneNumber = userRegistrationDTO.getPhoneNumber();
        String firstName = userRegistrationDTO.getFirstName();
        String lastName = userRegistrationDTO.getLastName();
        String password = userRegistrationDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

        Optional<User> userOld = userRepository.findByEmail(email);
        if(!userOld.isEmpty()){
            throw new EmailAlreadyRegisteredException(messageSource.getMessage(
                    "error.email.registered", new Object[]{email}, LocaleContextHolder.getLocale()));
        }

        if(!userOld.isEmpty()){
            throw new PhoneNumberAlreadyRegisteredException(messageSource.getMessage(
                    "error.phone.registered", new Object[]{phoneNumber}, LocaleContextHolder.getLocale()));
        }

        User user = new User();
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(encodedPassword);
        user.setRoleName(RoleName.USER);

        userRepository.save(user);

    }

    public String loginUser(UserLoginDTO userLoginDTO){

        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailDoesNotExistException(
                        messageSource.getMessage("error.email.does.not.exist", new Object[]{email},
                                LocaleContextHolder.getLocale())
                ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException(
                    messageSource.getMessage("error.password.incorrect", new Object[]{},
                            LocaleContextHolder.getLocale()
                    )
            );
        }

        return jwtService.generateToken(user);
    }

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

    public void createUser(UserRegistrationDTO dto) {
        registerUser(dto);
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

    public boolean changePassword(UUID id, String newPassword, String oldPassword){
        User user = getUserInfo(id);
        String password = user.getPassword();
        if(passwordEncoder.matches(oldPassword, password)){
            throw new IncorrectPasswordException(
                    messageSource.getMessage("error.password.change.failure", new Object[]{},
                            LocaleContextHolder.getLocale()));
        }
        String encodedPassword =passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        return true;
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

}
