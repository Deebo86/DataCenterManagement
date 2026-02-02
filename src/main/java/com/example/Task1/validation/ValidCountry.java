package com.example.Task1.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CountryFieldValidator.class)
@Documented
public @interface ValidCountry {
    String message() default "This country does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
