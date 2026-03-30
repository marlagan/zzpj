package com.zzpj.purrsuit.userservice.service;

import com.zzpj.purrsuit.userservice.dto.UserLoginDTO;
import com.zzpj.purrsuit.userservice.dto.UserRegistrationDTO;
import com.zzpj.purrsuit.userservice.entity.User;
import com.zzpj.purrsuit.userservice.exceptions.EmailAlreadyRegisteredException;
import com.zzpj.purrsuit.userservice.exceptions.EmailDoesNotExistException;
import com.zzpj.purrsuit.userservice.exceptions.IncorrectPasswordException;
import com.zzpj.purrsuit.userservice.exceptions.PhoneNumberAlreadyRegisteredException;
import com.zzpj.purrsuit.userservice.repository.RoleRepository;
import com.zzpj.purrsuit.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private final MessageSource messageSource;

    public void registerUser(UserRegistrationDTO userRegistrationDTO){

        String email = userRegistrationDTO.getEmail();
        String phoneNumber = userRegistrationDTO.getPhoneNumber();
        String firstName = userRegistrationDTO.getFirstName();
        String lastName = userRegistrationDTO.getLastName();
        String password = userRegistrationDTO.getPassword();

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
        user.setPassword(password);

        userRepository.save(user);

    }

    public void loginUser(UserLoginDTO userLoginDTO){

        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();
        User user = userRepository.findByEmail(email).get(0);

        if(userRepository.findByEmail(email).size() != 0){
            throw new EmailDoesNotExistException(messageSource.getMessage(
                    "error.email.does.not.exist", new Object[]{email}, LocaleContextHolder.getLocale()));
        }
        //temporary
        if(!user.getPassword().equals(password)){
            throw new IncorrectPasswordException(messageSource.getMessage(
                    "error.email.does.not.exist",  new Object[]{}, LocaleContextHolder.getLocale()));
        }

    }
}
