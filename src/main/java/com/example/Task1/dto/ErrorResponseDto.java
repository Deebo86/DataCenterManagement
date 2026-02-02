package com.example.Task1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponseDto {
    private int statusCode;
    private String path;
    private String errorName;
    private List<String> reasons;
    private String errorMessage;

}
