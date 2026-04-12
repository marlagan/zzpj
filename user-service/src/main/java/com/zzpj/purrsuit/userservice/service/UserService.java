package com.zzpj.purrsuit.userservice.service;

import com.zzpj.purrsuit.userservice.dto.UserLoginDTO;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.entity.User;
import com.zzpj.purrsuit.userservice.enums.RoleName;
import com.zzpj.purrsuit.userservice.exceptions.*;
import com.zzpj.purrsuit.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.catalina.LifecycleState;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

        if(userRepository.findByEmail(email).size() != 0){
            throw new EmailAlreadyRegisteredException(messageSource.getMessage(
                    "error.email.registered", new Object[]{email}, LocaleContextHolder.getLocale()));
        }

        if(userRepository.findByPhoneNumber(phoneNumber).size() != 0){
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
        User user = userRepository.findByEmail(email).get(0);

        if(userRepository.findByEmail(email).size() != 0){
            throw new EmailDoesNotExistException(messageSource.getMessage(
                    "error.email.does.not.exist", new Object[]{email},
                    LocaleContextHolder.getLocale()));
        }

        if(passwordEncoder.matches(password, user.getPassword())){
            throw new IncorrectPasswordException(messageSource.getMessage(
                    "error.password.incorrect",  new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        return jwtService.generateToken(user);
    }

    public User getUserInfo(Long id) {

        if (!userRepository.existsById(id)){
            throw new NoUserFoundException(
                    messageSource.getMessage("error.user.not.found", new Object[]{id},
                            LocaleContextHolder.getLocale()));
        }

        return userRepository.findById(id).get();
    }

    public void createUser(UserRegistrationDTO dto) {
        registerUser(dto);
    }

    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)){
            throw new NoUserFoundException(
                    messageSource.getMessage("error.user.not.found", new Object[]{id},
                            LocaleContextHolder.getLocale()));
        }

        userRepository.deleteById(id);
    }

    public void changeRole(Long id, RoleName role) {

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

}
