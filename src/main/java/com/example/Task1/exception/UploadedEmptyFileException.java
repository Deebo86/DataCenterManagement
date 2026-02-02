package com.example.Task1.exception;

public class UploadedEmptyFileException extends RuntimeException {
    public UploadedEmptyFileException(String message) {
        super(message);
    }
}
