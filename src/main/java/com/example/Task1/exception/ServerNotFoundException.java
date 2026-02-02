package com.example.Task1.exception;

public class ServerNotFoundException extends NotFoundException {
    public ServerNotFoundException() {
    }

    public ServerNotFoundException(String message) {
        super(message);
    }
}
