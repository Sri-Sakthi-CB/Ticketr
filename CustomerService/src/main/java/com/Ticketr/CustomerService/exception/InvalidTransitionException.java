package com.Ticketr.CustomerService.exception;

import org.springframework.http.HttpStatus;

public class InvalidTransitionException extends ApiException {
    public InvalidTransitionException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
