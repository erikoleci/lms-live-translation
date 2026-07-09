package com.tili.livetranslation.exception;

public class SessionFullException extends RuntimeException {
    public SessionFullException(String message) {
        super(message);
    }
}
