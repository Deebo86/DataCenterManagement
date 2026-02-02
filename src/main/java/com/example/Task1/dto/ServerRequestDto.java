package com.example.Task1.dto;

import com.example.Task1.model.enums.OperatingSystem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerRequestDto {
    @NotNull
    @Min(10)
    private Integer memory;
    @NotNull
    @Max(9)
    @Min(1)
    private Integer noOfCPU;
    @NotNull
    private OperatingSystem os;
}
