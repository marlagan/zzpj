package com.zzpj.purrsuit.userservice.exceptions;

public class EmailDoesNotExistException extends RuntimeException {
    public EmailDoesNotExistException(String message) {
        super(message);
    }
}
