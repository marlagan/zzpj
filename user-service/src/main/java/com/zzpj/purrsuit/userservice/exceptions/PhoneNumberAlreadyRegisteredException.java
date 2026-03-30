package com.zzpj.purrsuit.userservice.exceptions;

public class PhoneNumberAlreadyRegisteredException extends RuntimeException {

    public PhoneNumberAlreadyRegisteredException(String message) {
        super(message);
    }
}
