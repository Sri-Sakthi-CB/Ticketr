package com.Ticketr.CustomerService.exception;

import org.springframework.http.HttpStatus;

/** Raised whenever a user attempts an action they are not the designated stakeholder for — e.g. closing a ticket they aren't assigned to. */
public class ForbiddenActionException extends ApiException {
    public ForbiddenActionException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
