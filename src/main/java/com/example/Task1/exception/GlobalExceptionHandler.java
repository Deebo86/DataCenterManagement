package com.example.Task1.exception;

import com.example.Task1.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private String simplifyType(Class<?> type) {
        if (type == null) return "unknown";

        if (type == Integer.class || type == int.class) return "a number";
        if (type == Long.class || type == long.class) return "a long number";
        if (type == Boolean.class || type == boolean.class) return "true or false";
        if (type.isEnum()) return "one of: " + Arrays.toString(type.getEnumConstants());

        return type.getSimpleName();
    }

    //EXCEPTIONS
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAnyException(Exception ex, HttpServletRequest req) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorName(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .errorMessage(ex.getMessage())
                .build();

        log.error("fallback exception handler", ex);

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundEntityException(NotFoundException ex, HttpServletRequest req) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .errorName(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .errorMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotAllServersFoundException.class)
    public ResponseEntity<ErrorResponseDto> handlePartialEntityNotFoundException(NotAllServersFoundException ex, HttpServletRequest req) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .errorName(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CurrentCapacityOverMaxCapacityException.class)
    public ResponseEntity<ErrorResponseDto> handleCurrentCapacityOverMaxCapacityException(CurrentCapacityOverMaxCapacityException ex, HttpServletRequest req) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .errorName(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .errorMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UploadedEmptyFileException.class)
    public ResponseEntity<ErrorResponseDto> handleFileUploadException(UploadedEmptyFileException ex, HttpServletRequest req) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorName(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .errorMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handleFileProcessingException(FileProcessingException ex, HttpServletRequest req) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorName(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .errorMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    //VALIDATION VIOLATION
    //Path Variable validation violation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest req) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorName(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .errorMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //for path variable type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        List<String> reasons = new ArrayList<>();

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s.",
                ex.getValue(),
                ex.getName(),
                simplifyType(ex.getRequiredType())
        );

        reasons.add(message);

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .path(req.getRequestURI())
                .errorName(ex.getClass().getSimpleName())
                .reasons(reasons).build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //called for validation violations in request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> reasons = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format(
                        "Validation failed for field '%s' of object '%s', value %s",
                        error.getField(),
                        error.getObjectName(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .path(req.getRequestURI())
                .errorName(ex.getClass().getSimpleName())
                .reasons(reasons).build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodValidationException(HandlerMethodValidationException ex, HttpServletRequest req) {
        /*
        ex.getParameterValidationResults btraga3 list feeha list of validations for each method argument; a list of parameterValidationResult lists
        kol parameterValidationResult is a list of validation errors; getResolvableErrors().stream() -> dee awel lambda function parameterValidationResult -> { ...} .
        l kol list of validation errors b2a hntala3 el message zat nafsaha -> dee tany lambda return parameterValidationResult.getResolvableErrors().stream()
                            .map(error -> {...}
         */
        List<String> reasons = ex.getParameterValidationResults().stream() // Get all validation results
                .flatMap(parameterValidationResult -> { //flattens all the validation results into one stream (rope) we can work with
                    //each parameterValidationResult represents validations for 1 method argument
                    String parameterName = parameterValidationResult.getMethodParameter().getParameterName();
                    return parameterValidationResult.getResolvableErrors().stream() // Get errors for this parameter
                            .map(error -> {
                                if (error instanceof ConstraintViolation<?> violation) {
                                    // For field-level violations, you can often get the specific field
                                    String field = violation.getPropertyPath().toString() != null ? violation.getPropertyPath().toString() : parameterName;
                                    return field + ": " + violation.getMessage();
                                } else if (error instanceof FieldError fieldError) {
                                    // In some cases, it might still be a FieldError
                                    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                                }
                                return (parameterName != null ? parameterName + ": " : "") + error.getDefaultMessage();
                            });
                })
                .toList();

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .path(req.getRequestURI())
                .errorName(ex.getClass().getSimpleName())
                .reasons(reasons).build();

        log.error("fallback exception handler", ex);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    //type parsing mismatch
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleJsonParsingErrors(HttpMessageNotReadableException ex, HttpServletRequest req) {
        Throwable cause = ex.getCause();
        List<String> reasons = new ArrayList<>();

        if (cause instanceof JsonMappingException) {
            JsonMappingException jme = (JsonMappingException) cause;

            for (JsonMappingException.Reference ref : jme.getPath()) {
                String field = ref.getFieldName();
                String description = String.format(
                        "Deserialization failed for field '%s': %s",
                        field,
                        jme.getOriginalMessage()
                );
                reasons.add(description);
            }
        } else {
            reasons.add("Malformed JSON request: " + ex.getMessage());
        }

        ErrorResponseDto error = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .path(req.getRequestURI())
                .errorName(ex.getClass().getSimpleName())
                .reasons(reasons).build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
