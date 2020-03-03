package com.movemoney.storage;

public class TransactionException extends RuntimeException {

    private static final long serialVersionUID = 7718828512143293558L;
    private final ExeptionErrorCode code;

    public TransactionException(ExeptionErrorCode code) {
        super();
        this.code = code;
    }

    public TransactionException(String message, Throwable cause, ExeptionErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public TransactionException(String message, ExeptionErrorCode code) {
        super(message);
        this.code = code;
    }

    public TransactionException(Throwable cause, ExeptionErrorCode code) {
        super(cause);
        this.code = code;
    }

    public ExeptionErrorCode getCode() {
        return this.code;
    }
}
