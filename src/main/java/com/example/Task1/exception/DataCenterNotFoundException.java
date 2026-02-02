package com.example.Task1.exception;

public class DataCenterNotFoundException extends NotFoundException{
    public DataCenterNotFoundException() {
    }

    public DataCenterNotFoundException(String message) {
        super(message);
    }
}
