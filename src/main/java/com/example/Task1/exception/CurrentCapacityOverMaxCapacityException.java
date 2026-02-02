package com.example.Task1.exception;

public class CurrentCapacityOverMaxCapacityException extends RuntimeException {
    public CurrentCapacityOverMaxCapacityException(String message) {
        super(message);
    }

    public CurrentCapacityOverMaxCapacityException() {
    }
}
