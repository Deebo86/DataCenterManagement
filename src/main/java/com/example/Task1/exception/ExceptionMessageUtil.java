package com.example.Task1.exception;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExceptionMessageUtil {

//public static String extractReadableMessage(HttpMessageNotReadableException ex) {
//        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
//                .map(error -> String.format(
//                        "Validation failed for field '%s' of object '%s', value %s",
//                        error.getField(),
//                        error.getObjectName(),
//                        error.getDefaultMessage()
//                ))
//                .collect(Collectors.toList());
//
//        String combinedMessage = String.join("; ", messages);
//
//    }
}

