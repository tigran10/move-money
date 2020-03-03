package com.movemoney.ex;

import com.movemoney.storage.ExeptionErrorCode;

public class MoveMoneyExceptionWithCode extends RuntimeException {

    private static final long serialVersionUID = 7718828512143293558L;
    private final ExeptionErrorCode code;

    public MoveMoneyExceptionWithCode(ExeptionErrorCode code) {
        super();
        this.code = code;
    }

    public MoveMoneyExceptionWithCode(String message, Throwable cause, ExeptionErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public MoveMoneyExceptionWithCode(String message, ExeptionErrorCode code) {
        super(message);
        this.code = code;
    }

    public MoveMoneyExceptionWithCode(Throwable cause, ExeptionErrorCode code) {
        super(cause);
        this.code = code;
    }

    public ExeptionErrorCode getCode() {
        return this.code;
    }
}
