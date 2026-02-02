package com.example.Task1.exception;

public class NotAllServersFoundException extends RuntimeException {
    public NotAllServersFoundException() {
    }

    public NotAllServersFoundException(String message) {
        super(message);
    }
}
