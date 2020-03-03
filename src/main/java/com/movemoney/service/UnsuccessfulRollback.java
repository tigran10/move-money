package com.movemoney.service;

import com.movemoney.ex.MoveMoneyExceptionWithCode;
import com.movemoney.storage.ExeptionErrorCode;

public class UnsuccessfulRollback extends MoveMoneyExceptionWithCode {

    public UnsuccessfulRollback(ExeptionErrorCode code) {
        super(code);
    }

    public UnsuccessfulRollback(String message, Throwable cause, ExeptionErrorCode code) {
        super(message, cause, code);
    }

    public UnsuccessfulRollback(String message, ExeptionErrorCode code) {
        super(message, code);
    }

    public UnsuccessfulRollback(Throwable cause, ExeptionErrorCode code) {
        super(cause, code);
    }
}
