package com.movemoney.service;

import com.movemoney.ex.MoveMoneyExceptionWithCode;
import com.movemoney.storage.ExeptionErrorCode;

public class BadUserRequestException extends MoveMoneyExceptionWithCode {

    public BadUserRequestException(ExeptionErrorCode code) {
        super(code);
    }

    public BadUserRequestException(String message, Throwable cause, ExeptionErrorCode code) {
        super(message, cause, code);
    }

    public BadUserRequestException(String message, ExeptionErrorCode code) {
        super(message, code);
    }

    public BadUserRequestException(Throwable cause, ExeptionErrorCode code) {
        super(cause, code);
    }
}
