package com.example.Task1.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

}
