package com.example.Task1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class CountryFieldValidator implements ConstraintValidator<ValidCountry, String> {
    private List<String> countries = new ArrayList<>(List.of("egypt",
            "united states of america",
            "canada",
            "germany",
            "france",
            "sweden",
            "switzerland",
            "belgium",
            "united kingdom",
            "poland",
            "netherlands",
            "denmark",
            "hungary",
            "prague",
            "italy",
            "spain",
            "china",
            "korean"));

    @Override
    public boolean isValid(String country, ConstraintValidatorContext constraintValidatorContext) {
        if (country == null) return true;
        return countries.contains(country.toLowerCase());
    }
}
