package com.example.Task1.dto;

import com.example.Task1.model.enums.OperatingSystem;
import jakarta.validation.constraints.*;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerUpdateRequestDto {
    @Min(10)
    private Integer memory;
    @Max(9)
    private Integer noOfCPU;
    private OperatingSystem os;
}
