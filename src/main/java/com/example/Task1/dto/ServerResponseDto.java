package com.example.Task1.dto;

import com.example.Task1.model.enums.OperatingSystem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerResponseDto {
    private int id;
    private int datacenterId;
    private Integer memory;
    private Integer noOfCPU;
    private OperatingSystem os;
}
